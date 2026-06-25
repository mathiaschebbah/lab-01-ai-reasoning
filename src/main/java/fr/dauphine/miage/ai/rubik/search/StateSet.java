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

    /** Keeps the smallest {@code g} seen for the configuration. */
    public void add(State state) {
        Integer current = bestCostByState.get(state);
        if (current == null || state.getNbrActions() < current) {
            bestCostByState.put(state, state.getNbrActions());
        }
    }

    public boolean contains(State state) {
        return bestCostByState.containsKey(state);
    }

    /** @return the best known expanded cost, or {@link Integer#MAX_VALUE} if never expanded. */
    public int bestCost(State state) {
        return bestCostByState.getOrDefault(state, Integer.MAX_VALUE);
    }

    public int size() {
        return bestCostByState.size();
    }

    /** Empties the explored set so the same search object can be reused. */
    public void clear() {
        bestCostByState.clear();
    }
}
