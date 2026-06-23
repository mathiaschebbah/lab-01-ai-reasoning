package fr.dauphine.miage.ai.rubik.heuristic;

import fr.dauphine.miage.ai.rubik.search.State;

/**
 * The trivial heuristic that always returns zero.
 *
 * <p>With {@code h(n) = 0} the A* evaluation {@code f = g + h} reduces to
 * {@code f = g}, so A* behaves exactly as a uniform cost search. This is the
 * heuristic used for the first part of the lab, where the statement asks to
 * "ignore the heuristic" and run a plain uniform cost search before adding a
 * real, guiding heuristic.</p>
 *
 * <p>It is trivially admissible: zero never overestimates a non negative real
 * cost.</p>
 */
public final class ZeroHeuristic implements Heuristic {

    @Override
    public int value(State state) {
        return 0;
    }
}
