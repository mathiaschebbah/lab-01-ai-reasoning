package fr.dauphine.miage.ai.rubik.search;

import java.util.HashMap;
import java.util.Map;

/**
 * The set of states already expanded by the search, called {@code explored} in
 * the statement.
 *
 * <p>As required it offers {@link #add(State)} to record a state and
 * {@link #contains(State)} to test membership. States are compared by cube
 * configuration (see {@link State#equals(Object)}), so two different paths that
 * reach the same configuration count as the same explored state.</p>
 *
 * <p>In addition to plain membership, the set remembers the smallest cost
 * {@code g} with which each configuration has been expanded. The A* graph search
 * uses {@link #bestCost(State)} to discard a state popped from the frontier when
 * a cheaper path to the same configuration has already been expanded.</p>
 */
public final class StateSet {

    private final Map<State, Integer> bestCostByState = new HashMap<>();

    /**
     * Adds a state to the explored set, keeping the smallest {@code g} seen for
     * its configuration.
     *
     * @param state the state to record
     */
    public void add(State state) {
        Integer current = bestCostByState.get(state);
        if (current == null || state.getNbrActions() < current) {
            bestCostByState.put(state, state.getNbrActions());
        }
    }

    /**
     * Returns whether a state with the same configuration has already been
     * expanded.
     *
     * @param state the state to test
     * @return {@code true} if the configuration is in the explored set
     */
    public boolean contains(State state) {
        return bestCostByState.containsKey(state);
    }

    /**
     * Returns the smallest {@code g} with which the given configuration has been
     * expanded, or {@link Integer#MAX_VALUE} if it has never been expanded.
     *
     * @param state the state to look up
     * @return the best known expanded cost for this configuration
     */
    public int bestCost(State state) {
        return bestCostByState.getOrDefault(state, Integer.MAX_VALUE);
    }

    /** @return the number of distinct configurations expanded so far. */
    public int size() {
        return bestCostByState.size();
    }

    /** Empties the explored set so the same search object can be reused. */
    public void clear() {
        bestCostByState.clear();
    }
}
