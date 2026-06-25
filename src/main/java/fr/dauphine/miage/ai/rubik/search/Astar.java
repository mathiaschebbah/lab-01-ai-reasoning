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

    private long expandedCount;

    private long generatedCount;

    /**
     * Largest size the frontier reached during the last solve. This is the memory
     * the search needs: Lecture 2 notes that A* runs out of space long before it
     * runs out of time, so the peak frontier is the metric that matters most for
     * the deeper scrambles.
     */
    private long peakFrontierSize;

    /**
     * Largest size the explored (closed) set reached during the last solve, that
     * is the number of distinct configurations recorded as expanded. It grows by
     * one each time a fresh node is added to the closed set.
     */
    private long peakExploredSize;

    public Astar(Cube initial) {
        this.root = new State(initial.copy(), 0, null, -1);
        this.explored = new StateSet();
        this.frontier = new PriorityQueueState();
    }

    public void setExpansionLimit(long limit) {
        this.expansionLimit = limit;
    }

    /** @param millis the budget in milliseconds, or {@code 0} to disable it. */
    public void setTimeBudgetMillis(long millis) {
        this.timeBudgetMillis = millis;
    }

    public Outcome getLastOutcome() {
        return lastOutcome;
    }

    public void setPruneInverseMoves(boolean prune) {
        this.pruneInverseMoves = prune;
    }

    public long getExpandedCount() {
        return expandedCount;
    }

    public long getGeneratedCount() {
        return generatedCount;
    }

    public long getPeakFrontierSize() {
        return peakFrontierSize;
    }

    public long getPeakExploredSize() {
        return peakExploredSize;
    }

    /**
     * @return the action strings from the root to the solved cube, an empty list
     *         if the root is already solved, or {@code null} if no solution is
     *         found within the configured limits
     */
    public List<String> solve() {
        expandedCount = 0;
        generatedCount = 0;
        peakFrontierSize = 0;
        peakExploredSize = 0;
        lastOutcome = Outcome.EXHAUSTED;

        // Reset the search structures so the same Astar object can solve again.
        frontier.clear();
        explored.clear();

        // Wall-clock deadline, only computed when a budget was requested. The
        // System.nanoTime() check is throttled below to stay cheap.
        final boolean timed = timeBudgetMillis > 0;
        final long deadline = timed
                ? System.nanoTime() + timeBudgetMillis * 1_000_000L
                : 0L;

        // The root is the start node, not a generated successor, so it is not
        // counted in generatedCount. Seeding the peak here makes the frontier
        // metric correct even when the search exits immediately (solved root,
        // node limit or time budget reached on the first pop).
        frontier.push(root);
        peakFrontierSize = frontier.size();

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
            // The explored set never shrinks, so its current size is its peak.
            peakExploredSize = explored.size();

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
                // Count a child as generated only when it actually enters the
                // frontier; a duplicate rejected by push is not a new node.
                if (frontier.push(child)) {
                    generatedCount++;
                }
            }
            if (frontier.size() > peakFrontierSize) {
                peakFrontierSize = frontier.size();
            }
        }
        return null; // Frontier exhausted without reaching the goal.
    }

    private List<String> reconstructPath(State goal) {
        LinkedList<String> actions = new LinkedList<>();
        State current = goal;
        while (current.getPere() != null) {
            actions.addFirst(Move.notation(current.getActionPere()));
            current = current.getPere();
        }
        return actions;
    }

    private static boolean isInverseOfParentMove(State node, State child) {
        int parentMove = node.getActionPere();
        if (parentMove < 0) {
            return false; // The root has no move to undo.
        }
        return child.getActionPere() == Move.inverse(parentMove);
    }

    public State getRoot() {
        return root;
    }

    public static String notationOf(int action) {
        return Move.notation(action);
    }
}
