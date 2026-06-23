package fr.dauphine.miage.ai.rubik.heuristic;

import fr.dauphine.miage.ai.rubik.search.State;

/**
 * A heuristic function used to guide the A* search.
 *
 * <p>As seen in Lecture 2, a heuristic {@code h(n)} estimates the cost of the
 * cheapest path from a state to the goal. To keep A* optimal the heuristic must
 * be admissible, that is, it must never overestimate that real cost.</p>
 *
 * <p>The two concrete heuristics of this project are both built from relaxations
 * of the Rubik's Cube problem, exactly as the statement requires:</p>
 *
 * <ul>
 *     <li>{@link HeuristicLabel} assumes each sticker moves independently;</li>
 *     <li>{@link HeuristicCube} assumes each cube piece moves independently.</li>
 * </ul>
 */
public interface Heuristic {

    /**
     * Computes the heuristic value of a state.
     *
     * @param state the state to evaluate
     * @return a non negative estimate of the remaining number of moves
     */
    int value(State state);
}
