package fr.dauphine.miage.ai.rubik.util;

import fr.dauphine.miage.ai.rubik.model.Cube;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Produces random scrambles of a cube.
 *
 * <p>A scramble applies a sequence of random quarter turns. To obtain a more
 * realistic scramble (and a deeper effective distance for a given length), the
 * generator never applies the exact inverse of the previous move, which would
 * cancel it out. The exact list of applied actions is returned so that callers
 * can report or replay the scramble.</p>
 */
public final class Scrambler {

    private final Random random;

    /** Creates a scrambler with a fixed seed for reproducibility. */
    public Scrambler(long seed) {
        this.random = new Random(seed);
    }

    /** Creates a scrambler seeded from the system clock. */
    public Scrambler() {
        this.random = new Random();
    }

    /**
     * Scrambles the given cube in place with the requested number of moves and
     * returns the list of applied action identifiers.
     *
     * @param cube  the cube to scramble (modified in place)
     * @param moves the number of random quarter turns to apply
     * @return the list of applied action identifiers (each 0 to 11)
     */
    public List<Integer> scramble(Cube cube, int moves) {
        List<Integer> applied = new ArrayList<>(moves);
        int previous = -1;
        for (int i = 0; i < moves; i++) {
            int action;
            do {
                action = random.nextInt(12);
            } while (previous >= 0 && action == inverse(previous));
            cube.applyAction(action);
            applied.add(action);
            previous = action;
        }
        return applied;
    }

    /**
     * Returns a freshly scrambled cube.
     *
     * @param moves the number of random quarter turns to apply
     * @return a new scrambled cube
     */
    public Cube scrambledCube(int moves) {
        Cube cube = new Cube();
        scramble(cube, moves);
        return cube;
    }

    private static int inverse(int action) {
        return action < 6 ? action + 6 : action - 6;
    }
}
