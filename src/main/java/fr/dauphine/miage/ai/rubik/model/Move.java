package fr.dauphine.miage.ai.rubik.model;

/**
 * Central definition of the twelve cube moves and their notation.
 *
 * <p>This single place encodes the move numbering of the statement, so the
 * scrambler, the search and the interface never re-implement the same facts (the
 * action to notation mapping, or the fact that move {@code i} and move
 * {@code i + 6} are inverses).</p>
 *
 * <pre>
 * 0 turnUp     U   | 6  returnUp    U'
 * 1 turnLeft   L   | 7  returnLeft  L'
 * 2 turnFront  F   | 8  returnFront F'
 * 3 turnRight  R   | 9  returnRight R'
 * 4 turnDown   D   | 10 returnDown  D'
 * 5 turnBack   B   | 11 returnBack  B'
 * </pre>
 */
public final class Move {

    /** Number of distinct moves (twelve quarter turns). */
    public static final int COUNT = 12;

    /** Human readable notation for each action identifier 0 to 11. */
    private static final String[] NOTATION = {
            "U", "L", "F", "R", "D", "B",       // 0..5  clockwise
            "U'", "L'", "F'", "R'", "D'", "B'"  // 6..11 counter clockwise
    };

    private Move() {
        // Utility class.
    }

    /**
     * Returns the notation of an action identifier.
     *
     * @param action the action identifier (0 to 11)
     * @return the matching notation, for example "R" or "U'"
     */
    public static String notation(int action) {
        return NOTATION[action];
    }

    /**
     * Returns the action identifier of a notation string.
     *
     * @param notation one of "U", "L", ..., "B'"
     * @return the matching action identifier (0 to 11)
     * @throws IllegalArgumentException if the notation is unknown
     */
    public static int fromNotation(String notation) {
        for (int action = 0; action < NOTATION.length; action++) {
            if (NOTATION[action].equals(notation)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown move notation: " + notation);
    }

    /**
     * Returns the inverse of an action: the move that undoes it. A clockwise turn
     * (0 to 5) and its counter clockwise counterpart (6 to 11) are inverses.
     *
     * @param action the action identifier (0 to 11)
     * @return the inverse action identifier
     */
    public static int inverse(int action) {
        return action < 6 ? action + 6 : action - 6;
    }
}
