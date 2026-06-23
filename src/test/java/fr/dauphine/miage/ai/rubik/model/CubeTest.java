package fr.dauphine.miage.ai.rubik.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Algebraic and geometric invariants of the {@link Cube} model.
 *
 * <p>Because the move tables are generated from a 3D model, these tests act as
 * the proof that the generation is correct: a wrong rotation matrix or a flipped
 * strip would immediately break one of the invariants below.</p>
 */
class CubeTest {

    /** The twelve move codes paired so that index i and i+6 are inverses. */
    private static final int CLOCKWISE_START = 0;   // 0..5
    private static final int COUNTER_START = 6;      // 6..11

    @Test
    @DisplayName("A fresh cube is solved and each face shows its color")
    void freshCubeIsSolved() {
        Cube cube = new Cube();
        assertTrue(cube.isSolved());
        for (Face face : Face.values()) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    assertEquals(face.solvedColor().code(), cube.get(face, row, col),
                            "Face " + face + " sticker (" + row + "," + col + ")");
                }
            }
        }
    }

    @Test
    @DisplayName("Any single turn breaks the solved state")
    void singleTurnUnsolves() {
        for (int action = 0; action < 12; action++) {
            Cube cube = new Cube();
            cube.applyAction(action);
            assertFalse(cube.isSolved(), "Action " + action + " should unsolve the cube");
        }
    }

    @Test
    @DisplayName("turnX then returnX is the identity (inverse law)")
    void inverseLaw() {
        for (int i = 0; i < 6; i++) {
            Cube cube = scrambled(40, 123 + i);
            String before = cube.toCompactString();
            cube.applyAction(CLOCKWISE_START + i);
            cube.applyAction(COUNTER_START + i);
            assertEquals(before, cube.toCompactString(),
                    "Clockwise then counter clockwise of face index " + i);
        }
    }

    @Test
    @DisplayName("returnX then turnX is the identity")
    void inverseLawOtherWay() {
        for (int i = 0; i < 6; i++) {
            Cube cube = scrambled(40, 999 + i);
            String before = cube.toCompactString();
            cube.applyAction(COUNTER_START + i);
            cube.applyAction(CLOCKWISE_START + i);
            assertEquals(before, cube.toCompactString());
        }
    }

    @Test
    @DisplayName("Four identical clockwise turns return to the start (order four)")
    void orderFourLaw() {
        for (int i = 0; i < 6; i++) {
            Cube cube = scrambled(30, 7 + i);
            String before = cube.toCompactString();
            for (int k = 0; k < 4; k++) {
                cube.applyAction(CLOCKWISE_START + i);
            }
            assertEquals(before, cube.toCompactString(),
                    "Four clockwise turns of face index " + i);
        }
    }

    @Test
    @DisplayName("returnX equals three clockwise turnX")
    void returnEqualsThreeTurns() {
        for (int i = 0; i < 6; i++) {
            Cube viaReturn = new Cube();
            viaReturn.applyAction(COUNTER_START + i);

            Cube viaThree = new Cube();
            for (int k = 0; k < 3; k++) {
                viaThree.applyAction(CLOCKWISE_START + i);
            }
            assertEquals(viaThree.toCompactString(), viaReturn.toCompactString(),
                    "return of face index " + i + " should equal three turns");
        }
    }

    @Test
    @DisplayName("Every move preserves the count of each color (nine each)")
    void colorConservation() {
        Cube cube = scrambled(200, 2024);
        Map<Character, Integer> counts = new HashMap<>();
        String s = cube.toCompactString();
        for (int i = 0; i < s.length(); i++) {
            counts.merge(s.charAt(i), 1, Integer::sum);
        }
        assertEquals(6, counts.size(), "Exactly six distinct colors");
        for (Map.Entry<Character, Integer> e : counts.entrySet()) {
            assertEquals(9, e.getValue(), "Color " + e.getKey() + " must appear nine times");
        }
    }

    @Test
    @DisplayName("Centers never move under any face turn")
    void centersAreFixed() {
        for (int action = 0; action < 12; action++) {
            Cube cube = new Cube();
            cube.applyAction(action);
            for (Face face : Face.values()) {
                assertEquals(face.solvedColor().code(), cube.get(face, 1, 1),
                        "Center of " + face + " must stay fixed after action " + action);
            }
        }
    }

    @Test
    @DisplayName("The sexy move R U R' U' has order six")
    void sexyMoveOrderSix() {
        Cube cube = new Cube();
        // R U R' U' : actions 3, 0, 9, 6.
        for (int repeat = 0; repeat < 6; repeat++) {
            cube.applyAction(3);
            cube.applyAction(0);
            cube.applyAction(9);
            cube.applyAction(6);
            boolean solved = cube.isSolved();
            if (repeat < 5) {
                assertFalse(solved, "Sexy move should not solve before the sixth repetition (rep " + repeat + ")");
            } else {
                assertTrue(solved, "Sexy move must return to solved after exactly six repetitions");
            }
        }
    }

    @Test
    @DisplayName("A scramble followed by the reversed inverse sequence solves the cube")
    void scrambleThenInverseSolves() {
        Random random = new Random(42);
        int[] moves = new int[50];
        Cube cube = new Cube();
        for (int i = 0; i < moves.length; i++) {
            moves[i] = random.nextInt(12);
            cube.applyAction(moves[i]);
        }
        assertFalse(cube.isSolved());
        // Undo in reverse order, replacing each move by its inverse.
        for (int i = moves.length - 1; i >= 0; i--) {
            cube.applyAction(inverseAction(moves[i]));
        }
        assertTrue(cube.isSolved(), "Undoing every move in reverse must solve the cube");
    }

    @Test
    @DisplayName("Equal configurations are equal and hash equally")
    void equalityAndHashing() {
        Cube a = scrambled(20, 555);
        Cube b = scrambled(20, 555);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        b.applyAction(2);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("copy() produces an independent cube")
    void copyIsIndependent() {
        Cube original = scrambled(10, 1);
        Cube clone = original.copy();
        assertEquals(original, clone);
        clone.applyAction(5);
        assertNotEquals(original, clone, "Mutating the copy must not affect the original");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Cube scrambled(int moves, long seed) {
        Random random = new Random(seed);
        Cube cube = new Cube();
        for (int i = 0; i < moves; i++) {
            cube.applyAction(random.nextInt(12));
        }
        return cube;
    }

    /** Maps an action to its inverse action code. */
    private static int inverseAction(int action) {
        return action < 6 ? action + 6 : action - 6;
    }
}
