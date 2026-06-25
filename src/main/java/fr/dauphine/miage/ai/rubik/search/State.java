package fr.dauphine.miage.ai.rubik.search;

import fr.dauphine.miage.ai.rubik.heuristic.Heuristic;
import fr.dauphine.miage.ai.rubik.heuristic.ZeroHeuristic;
import fr.dauphine.miage.ai.rubik.model.Cube;
import java.util.LinkedList;

/**
 * A node of the search tree.
 *
 * <p>Each state corresponds to a cube configuration. As described in the
 * statement it stores:</p>
 *
 * <ul>
 *     <li>{@code cube}: the configuration represented by this node;</li>
 *     <li>{@code nbrActions}: the number of actions taken from the root, that is
 *         the path cost {@code g};</li>
 *     <li>{@code pere}: the parent state, used to rebuild the solution;</li>
 *     <li>{@code actionPere}: the action (0 to 11) that produced this state from
 *         its parent, also used to rebuild the solution;</li>
 *     <li>{@code valH}: the heuristic value, computed once when the state is
 *         created.</li>
 * </ul>
 *
 * <p>The heuristic used to fill {@code valH} is selected through the static
 * field {@link #heuristic}. Switching it (for instance from {@link ZeroHeuristic}
 * to a real heuristic) turns the search from a uniform cost search into a guided
 * A* search, exactly as the statement asks.</p>
 */
public final class State {

    /**
     * The heuristic used by every state to compute its {@code valH}. It defaults
     * to {@link ZeroHeuristic}, which makes A* behave as a uniform cost search.
     * Change it to {@code new HeuristicCube()} or {@code new HeuristicLabel()} to
     * guide the search.
     */
    public static Heuristic heuristic = new ZeroHeuristic();

    private final Cube cube;
    private final int nbrActions;
    private final State pere;
    private final int actionPere;
    private final int valH;
    private final int evaluation;

    /**
     * Builds a state.
     *
     * @param cube       the cube configuration of this node
     * @param nbrActions the number of actions from the root to this node
     * @param pere       the parent state, or {@code null} for the root
     * @param actionPere the action (0 to 11) used to reach this state, or -1 for
     *                   the root
     */
    public State(Cube cube, int nbrActions, State pere, int actionPere) {
        this.cube = cube;
        this.nbrActions = nbrActions;
        this.pere = pere;
        this.actionPere = actionPere;
        // The heuristic is computed last, once the other fields are set, because
        // a heuristic reads the cube of this state.
        this.valH = heuristic.value(this);
        // Cache f = g + h once; it is read many times during heap operations.
        this.evaluation = nbrActions + valH;
    }

    public Cube getCube() {
        return cube;
    }

    /** @return the number of actions from the root, that is the cost g. */
    public int getNbrActions() {
        return nbrActions;
    }

    /** @return the parent state, or {@code null} for the root. */
    public State getPere() {
        return pere;
    }

    /** @return the action used to reach this state, or -1 for the root. */
    public int getActionPere() {
        return actionPere;
    }

    public int getValH() {
        return valH;
    }

    /** @return the A* evaluation {@code f = g + h} used to order the frontier. */
    public int getEvaluation() {
        return evaluation;
    }

    public boolean isGoal() {
        return cube.isSolved();
    }

    /** Generates the twelve successors, one per rotation, in a LinkedList as the statement asks. */
    public LinkedList<State> expand() {
        LinkedList<State> children = new LinkedList<>();
        for (int action = 0; action < 12; action++) {
            Cube next = cube.withAction(action);
            children.add(new State(next, nbrActions + 1, this, action));
        }
        return children;
    }

    // ------------------------------------------------------------------
    // Two states are equal iff their cubes are equal. This identifies nodes by
    // configuration (not by path), which is what the explored set and the
    // frontier rely on to avoid revisiting the same configuration.
    // ------------------------------------------------------------------

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof State)) {
            return false;
        }
        return cube.equals(((State) other).cube);
    }

    @Override
    public int hashCode() {
        return cube.hashCode();
    }

    @Override
    public String toString() {
        return "State{g=" + nbrActions + ", h=" + valH + ", f=" + getEvaluation()
                + ", action=" + actionPere + "}";
    }
}
