package fr.dauphine.miage.ai.rubik.search;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * The frontier of the search, ordered by the A* evaluation {@code f = g + h}.
 *
 * <p>As described in the statement it offers {@link #push(State)} to add a state
 * and {@link #pop()} to retrieve the state with the smallest value (number of
 * actions plus heuristic). The {@code push} operation automatically checks
 * whether the same cube configuration is already in the frontier and, if so,
 * keeps only the better one (the smaller {@code f}).</p>
 *
 * <p>Java's {@link PriorityQueue} does not support an efficient decrease key, so
 * a better entry for an existing configuration is simply pushed on top while the
 * stale entry is marked obsolete through a {@code bestEvaluation} map. Obsolete
 * entries are skipped lazily when popped, which keeps every operation simple and
 * correct.</p>
 */
public final class PriorityQueueState {

    /**
     * Orders states by {@code f = g + h}; ties are broken in favor of the larger
     * {@code g} (the deeper node), which tends to drive the search toward goals
     * faster without affecting optimality.
     */
    private static final Comparator<State> BY_EVALUATION =
            Comparator.comparingInt(State::getEvaluation)
                    .thenComparing(Comparator.comparingInt(State::getNbrActions).reversed());

    private final PriorityQueue<State> queue = new PriorityQueue<>(BY_EVALUATION);

    /** Best (smallest) evaluation currently known for each configuration. */
    private final Map<State, Integer> bestEvaluation = new HashMap<>();

    /**
     * Adds a state to the frontier, keeping only the better entry when the same
     * configuration is already present.
     *
     * @param state the state to add
     */
    public void push(State state) {
        Integer known = bestEvaluation.get(state);
        if (known != null && known <= state.getEvaluation()) {
            // An equal or better entry for this configuration already exists.
            return;
        }
        bestEvaluation.put(state, state.getEvaluation());
        queue.add(state);
    }

    /**
     * Removes and returns the state with the smallest evaluation. Stale entries,
     * superseded by a better push for the same configuration, are skipped.
     *
     * @return the best state, or {@code null} if the frontier is empty
     */
    public State pop() {
        State candidate;
        while ((candidate = queue.poll()) != null) {
            Integer best = bestEvaluation.get(candidate);
            if (best != null && best == candidate.getEvaluation()) {
                // This is the live (best) entry for its configuration.
                bestEvaluation.remove(candidate);
                return candidate;
            }
            // Otherwise the entry is obsolete (a better one was pushed); skip it.
        }
        return null;
    }

    /**
     * Returns whether the same configuration is currently in the frontier.
     *
     * @param state the state to test
     * @return {@code true} if a live entry for this configuration exists
     */
    public boolean contains(State state) {
        return bestEvaluation.containsKey(state);
    }

    /** @return {@code true} if the frontier holds no live state. */
    public boolean isEmpty() {
        // The map holds exactly the live configurations.
        return bestEvaluation.isEmpty();
    }

    /** @return the number of live configurations in the frontier. */
    public int size() {
        return bestEvaluation.size();
    }
}
