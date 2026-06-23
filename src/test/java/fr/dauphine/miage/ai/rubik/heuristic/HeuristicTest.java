package fr.dauphine.miage.ai.rubik.heuristic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.dauphine.miage.ai.rubik.model.Cube;
import fr.dauphine.miage.ai.rubik.search.State;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the two relaxation heuristics.
 *
 * <p>The key property is admissibility: on a cube scrambled with {@code k}
 * random moves the heuristic must never exceed {@code k}, because {@code k} is an
 * upper bound on the optimal solution length (undoing the scramble takes at most
 * {@code k} moves). A heuristic value strictly greater than the optimal cost
 * would make A* lose optimality.</p>
 */
class HeuristicTest {

    @Test
    @DisplayName("Both heuristics return zero on a solved cube")
    void zeroOnSolved() {
        State solved = new State(new Cube(), 0, null, -1);
        assertEquals(0, new HeuristicLabel().value(solved));
        assertEquals(0, new HeuristicCube().value(solved));
        assertEquals(0, new ZeroHeuristic().value(solved));
    }

    @Test
    @DisplayName("Both heuristics are positive on a scrambled cube")
    void positiveOnScrambled() {
        State scrambled = scramble(10, 1);
        assertTrue(new HeuristicLabel().value(scrambled) > 0);
        assertTrue(new HeuristicCube().value(scrambled) > 0);
    }

    @Test
    @DisplayName("HeuristicLabel never exceeds the scramble length (admissible)")
    void labelAdmissible() {
        HeuristicLabel h = new HeuristicLabel();
        for (int k = 1; k <= 12; k++) {
            for (int seed = 0; seed < 20; seed++) {
                State s = scramble(k, seed * 31L + k);
                assertTrue(h.value(s) <= k,
                        "HeuristicLabel " + h.value(s) + " exceeds scramble length " + k);
            }
        }
    }

    @Test
    @DisplayName("HeuristicCube never exceeds the scramble length (admissible)")
    void cubeAdmissible() {
        HeuristicCube h = new HeuristicCube();
        for (int k = 1; k <= 12; k++) {
            for (int seed = 0; seed < 20; seed++) {
                State s = scramble(k, seed * 17L + k);
                assertTrue(h.value(s) <= k,
                        "HeuristicCube " + h.value(s) + " exceeds scramble length " + k);
            }
        }
    }

    @Test
    @DisplayName("A single quarter turn gives heuristic value at most one")
    void singleMoveBounded() {
        HeuristicLabel label = new HeuristicLabel();
        HeuristicCube cube = new HeuristicCube();
        for (int action = 0; action < 12; action++) {
            Cube c = new Cube();
            c.applyAction(action);
            State s = new State(c, 1, null, action);
            assertTrue(label.value(s) <= 1, "Label after one move should be <= 1");
            assertTrue(cube.value(s) <= 1, "Cube after one move should be <= 1");
        }
    }

    private static State scramble(int moves, long seed) {
        Random random = new Random(seed);
        Cube cube = new Cube();
        for (int i = 0; i < moves; i++) {
            cube.applyAction(random.nextInt(12));
        }
        return new State(cube, 0, null, -1);
    }
}
