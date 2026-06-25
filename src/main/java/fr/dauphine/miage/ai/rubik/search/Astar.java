package fr.dauphine.miage.ai.rubik.search;

import fr.dauphine.miage.ai.rubik.model.Cube;
import fr.dauphine.miage.ai.rubik.model.Move;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements the A* search algorithm to solve the Rubik's Cube, following the
 * pseudo code of Lecture 2.
 *
 * <p>The class holds the {@code root} state of the search tree, an
 * {@link #explored} set of expanded configurations and a {@link #frontier}
 * priority queue ordered by {@code f = g + h}. With {@link State#heuristic} set
 * to the zero heuristic the algorithm is a uniform cost search; with a real
 * heuristic it becomes a guided A* search.</p>
 *
 * <p>{@link #solve()} returns the sequence of actions, as strings (U, L, F, R,
 * D, B and their primed counterparts), that turns the initial configuration into
 * the solved cube. It returns an empty list when the cube is already solved and
 * {@code null} when no solution is found within the configured limits.</p>
 */
public final class Astar {

    private final State root;
    private final StateSet explored;
    private final PriorityQueueState frontier;

    /**
     * Maximum number of states expanded before giving up. A* on a Rubik's Cube
     * runs out of memory long before it runs out of time, so this guard keeps
     * the program responsive on configurations that are too scrambled to solve.
     */
    private long expansionLimit = 20_000_000L;

    /**
     * Optional wall-clock budget in milliseconds. When greater than zero, the
     * search gives up as soon as it has run for longer than this budget. It lets
     * a caller (the benchmark) bound the time spent on a hopeless configuration,
     * instead of waiting for the expansion limit to be reached. Zero disables it,
     * which keeps the default behavior unchanged.
     */
    private long timeBudgetMillis = 0L;

    /**
     * Why the last {@link #solve()} call stopped. It lets a caller tell an honest
     * "no solution" apart from a search that simply ran out of time or nodes.
     */
    public enum Outcome {
        /** A solution was found (or the cube was already solved). */
        SOLVED,
        /** The wall-clock time budget was exceeded. */
        TIMEOUT,
        /** The expansion (node) limit was reached. */
        NODE_LIMIT,
        /** The frontier emptied without reaching the goal (should not happen). */
        EXHAUSTED
    }

    /** Outcome of the last {@link #solve()} call. */
    private Outcome lastOutcome = Outcome.EXHAUSTED;

    /**
     * Optional optimization: when {@code true}, a successor whose action is the
     * exact inverse of the action that produced the current node is skipped (for
     * example, never apply U' right after U). Such a move would simply undo the
     * previous one and can never be part of an optimal solution, so pruning it
     * keeps optimality while shrinking the search tree. Disabled by default so
     * that {@link #solve()} matches the plain A* pseudo code of Lecture 2.
     */
    private boolean pruneInverseMoves = false;

    /** Number of states actually expanded by the last {@link #solve()} call. */
    private long expandedCount;

    /** Number of states generated (pushed to the frontier) by the last solve. */
    private long generatedCount;

    /**
     * Largest size the frontier reached during the last solve. This is the memory
     * the search needs: Lecture 2 notes that A* runs out of space long before it
     * runs out of time, so the peak frontier is the metric that matters most for
     * the deeper scrambles.
     */
    private long peakFrontierSize;

    /** Largest size the explored set reached during the last solve. */
    private long peakExploredSize;

    /**
     * Builds the search for a given initial cube configuration, which becomes the
     * root of the search tree.
     *
     * @param initial the cube configuration to solve
     */
    public Astar(Cube initial) {
        this.root = new State(initial.copy(), 0, null, -1);
        this.explored = new StateSet();
        this.frontier = new PriorityQueueState();
    }

    /** @param limit the maximum number of states to expand before failing. */
    public void setExpansionLimit(long limit) {
        this.expansionLimit = limit;
    }

    /**
     * Sets a wall-clock budget for the search.
     *
     * @param millis the budget in milliseconds, or {@code 0} to disable it
     */
    public void setTimeBudgetMillis(long millis) {
        this.timeBudgetMillis = millis;
    }

    /** @return why the last solve call stopped. */
    public Outcome getLastOutcome() {
        return lastOutcome;
    }

    /**
     * Enables or disables the inverse move pruning optimization.
     *
     * @param prune {@code true} to skip successors that immediately undo the
     *              previous move
     */
    public void setPruneInverseMoves(boolean prune) {
        this.pruneInverseMoves = prune;
    }

    /** @return the number of states expanded by the last solve call. */
    public long getExpandedCount() {
        return expandedCount;
    }

    /** @return the number of states generated by the last solve call. */
    public long getGeneratedCount() {
        return generatedCount;
    }

    /**
     * Returns the peak frontier size of the last solve call, that is the largest
     * number of states the priority queue held at once. It is the practical memory
     * footprint of the search.
     *
     * @return the largest frontier size observed during the last solve
     */
    public long getPeakFrontierSize() {
        return peakFrontierSize;
    }

    /**
     * Returns the peak explored set size of the last solve call, that is the
     * largest number of distinct configurations recorded as expanded.
     *
     * @return the largest explored set size observed during the last solve
     */
    public long getPeakExploredSize() {
        return peakExploredSize;
    }

    /**
     * Runs the A* algorithm from the root configuration.
     *
     * @return the list of action strings leading to the solved cube, an empty
     *         list if the root is already solved, or {@code null} if no solution
     *         is found within the expansion limit
     */
    public List<String> solve() {
        expandedCount = 0;
        generatedCount = 0;
        peakFrontierSize = 0;
        peakExploredSize = 0;
        lastOutcome = Outcome.EXHAUSTED;

        // Wall-clock deadline, only computed when a budget was requested. The
        // System.nanoTime() check is throttled below to stay cheap.
        final boolean timed = timeBudgetMillis > 0;
        final long deadline = timed
                ? System.nanoTime() + timeBudgetMillis * 1_000_000L
                : 0L;

        frontier.push(root);
        generatedCount++;

        while (!frontier.isEmpty()) {
            State node = frontier.pop();
            if (node == null) {
                break;
            }

            // Skip a configuration already expanded through a cheaper path.
            if (explored.contains(node) && explored.bestCost(node) <= node.getNbrActions()) {
                continue;
            }
            explored.add(node);
            if (explored.size() > peakExploredSize) {
                peakExploredSize = explored.size();
            }

            // Goal test on expansion guarantees optimality for A* graph search.
            if (node.isGoal()) {
                lastOutcome = Outcome.SOLVED;
                return reconstructPath(node);
            }

            if (expandedCount >= expansionLimit) {
                lastOutcome = Outcome.NODE_LIMIT;
                return null; // Give up: too scrambled to solve within the limit.
            }
            // Check the wall-clock budget every 4096 expansions to keep the
            // System.nanoTime() cost negligible compared to the search itself.
            if (timed && (expandedCount & 0xFFF) == 0 && System.nanoTime() >= deadline) {
                lastOutcome = Outcome.TIMEOUT;
                return null; // Give up: out of time on this configuration.
            }
            expandedCount++;

            for (State child : node.expand()) {
                if (pruneInverseMoves && isInverseOfParentMove(node, child)) {
                    continue; // Skip a move that would just undo the previous one.
                }
                if (explored.contains(child) && explored.bestCost(child) <= child.getNbrActions()) {
                    continue;
                }
                frontier.push(child);
                generatedCount++;
            }
            if (frontier.size() > peakFrontierSize) {
                peakFrontierSize = frontier.size();
            }
        }
        return null; // Frontier exhausted without reaching the goal.
    }

    /**
     * Rebuilds the sequence of actions from the root to the given goal state by
     * walking the {@code pere} chain and reading each {@code actionPere}.
     *
     * @param goal the goal state reached by the search
     * @return the ordered list of action strings from root to goal
     */
    private List<String> reconstructPath(State goal) {
        LinkedList<String> actions = new LinkedList<>();
        State current = goal;
        while (current.getPere() != null) {
            actions.addFirst(Move.notation(current.getActionPere()));
            current = current.getPere();
        }
        return actions;
    }

    /**
     * Returns whether the child was produced by the inverse of the move that
     * produced its parent node (for example child = U' applied after parent = U).
     *
     * @param node  the node being expanded
     * @param child one of its successors
     * @return {@code true} if the child's move undoes the node's move
     */
    private static boolean isInverseOfParentMove(State node, State child) {
        int parentMove = node.getActionPere();
        if (parentMove < 0) {
            return false; // The root has no move to undo.
        }
        return child.getActionPere() == Move.inverse(parentMove);
    }

    /** @return the root state of the search tree. */
    public State getRoot() {
        return root;
    }

    /**
     * Returns the notation string of an action identifier. Kept as a convenience
     * that delegates to {@link Move#notation(int)}.
     *
     * @param action the action identifier (0 to 11)
     * @return the matching notation (for example "R" or "U'")
     */
    public static String notationOf(int action) {
        return Move.notation(action);
    }
}
