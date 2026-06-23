package fr.dauphine.miage.ai.rubik.model;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * Models a 3x3x3 Rubik's Cube.
 *
 * <p>For each of the six faces (UP, DOWN, FRONT, BACK, LEFT, RIGHT) a 3x3 matrix
 * of characters stores the color of each sticker, exactly as described in the
 * lab statement. The possible characters are 'W', 'R', 'G', 'B', 'Y' and 'O'.
 * Row and column indices run from 0 to 2.</p>
 *
 * <p>The six {@code turn*} methods perform clockwise quarter turns of the
 * corresponding face; the six {@code return*} methods perform the counter
 * clockwise turns. {@link #isSolved()} returns {@code true} only when every face
 * is uniformly filled with its solved color.</p>
 *
 * <p>The actual sticker movements are computed from the permutation tables of
 * {@link CubeGeometry}, which derives them from a real three dimensional model.
 * Internally the 54 stickers are kept in a flat {@code char[54]} array indexed
 * by {@code face.ordinal() * 9 + row * 3 + col}; the per face matrices are a
 * view over this array exposed through {@link #get(Face, int, int)}.</p>
 */
public final class Cube {

    /** Precomputed clockwise permutation tables, one per face. */
    private static final Map<Face, int[]> CLOCKWISE = new EnumMap<>(Face.class);
    /** Precomputed counter clockwise permutation tables, one per face. */
    private static final Map<Face, int[]> COUNTER_CLOCKWISE = new EnumMap<>(Face.class);

    static {
        for (Face face : Face.values()) {
            int[] cw = CubeGeometry.clockwisePermutation(face);
            CLOCKWISE.put(face, cw);
            COUNTER_CLOCKWISE.put(face, invert(cw));
        }
    }

    /** Flat array of the 54 sticker color codes. */
    private final char[] stickers;

    /** Creates a solved cube. */
    public Cube() {
        this.stickers = new char[CubeGeometry.STICKER_COUNT];
        for (Face face : Face.values()) {
            char code = face.solvedColor().code();
            for (int cell = 0; cell < 9; cell++) {
                stickers[face.ordinal() * 9 + cell] = code;
            }
        }
    }

    /** Private copy constructor. */
    private Cube(char[] stickers) {
        this.stickers = stickers.clone();
    }

    /** @return a deep copy of this cube. */
    public Cube copy() {
        return new Cube(this.stickers);
    }

    /**
     * Returns the color of the sticker at the given face position.
     *
     * @param face the face to read
     * @param row  the row index (0 to 2)
     * @param col  the column index (0 to 2)
     * @return the color code stored there
     */
    public char get(Face face, int row, int col) {
        return stickers[CubeGeometry.index(face, row, col)];
    }

    /**
     * Sets the color of the sticker at the given face position. Mainly used by
     * the interface to build custom configurations.
     *
     * @param face the face to write
     * @param row  the row index (0 to 2)
     * @param col  the column index (0 to 2)
     * @param code the color code to store
     */
    public void set(Face face, int row, int col, char code) {
        stickers[CubeGeometry.index(face, row, col)] = code;
    }

    /**
     * Returns whether the cube is solved, that is, every face is uniformly
     * filled with a single color. We do not require a specific color per face,
     * only uniformity, which is equivalent up to a whole cube rotation and makes
     * the solver independent of the chosen orientation.
     *
     * @return {@code true} if the configuration is solved
     */
    public boolean isSolved() {
        for (Face face : Face.values()) {
            int base = face.ordinal() * 9;
            char center = stickers[base + 4];
            for (int cell = 0; cell < 9; cell++) {
                if (stickers[base + cell] != center) {
                    return false;
                }
            }
        }
        return true;
    }

    // ---------------------------------------------------------------------
    // Clockwise turns
    // ---------------------------------------------------------------------

    /** Turns the UP face clockwise. */
    public void turnUp() {
        apply(CLOCKWISE.get(Face.UP));
    }

    /** Turns the LEFT face clockwise. */
    public void turnLeft() {
        apply(CLOCKWISE.get(Face.LEFT));
    }

    /** Turns the FRONT face clockwise. */
    public void turnFront() {
        apply(CLOCKWISE.get(Face.FRONT));
    }

    /** Turns the RIGHT face clockwise. */
    public void turnRight() {
        apply(CLOCKWISE.get(Face.RIGHT));
    }

    /** Turns the DOWN face clockwise. */
    public void turnDown() {
        apply(CLOCKWISE.get(Face.DOWN));
    }

    /** Turns the BACK face clockwise. */
    public void turnBack() {
        apply(CLOCKWISE.get(Face.BACK));
    }

    // ---------------------------------------------------------------------
    // Counter clockwise turns
    // ---------------------------------------------------------------------

    /** Turns the UP face counter clockwise. */
    public void returnUp() {
        apply(COUNTER_CLOCKWISE.get(Face.UP));
    }

    /** Turns the LEFT face counter clockwise. */
    public void returnLeft() {
        apply(COUNTER_CLOCKWISE.get(Face.LEFT));
    }

    /** Turns the FRONT face counter clockwise. */
    public void returnFront() {
        apply(COUNTER_CLOCKWISE.get(Face.FRONT));
    }

    /** Turns the RIGHT face counter clockwise. */
    public void returnRight() {
        apply(COUNTER_CLOCKWISE.get(Face.RIGHT));
    }

    /** Turns the DOWN face counter clockwise. */
    public void returnDown() {
        apply(COUNTER_CLOCKWISE.get(Face.DOWN));
    }

    /** Turns the BACK face counter clockwise. */
    public void returnBack() {
        apply(COUNTER_CLOCKWISE.get(Face.BACK));
    }

    /**
     * Applies one of the twelve moves identified by its integer code, following
     * the table given in the lab statement (0 to 11).
     *
     * @param action the action identifier between 0 and 11
     */
    public void applyAction(int action) {
        switch (action) {
            case 0:  turnUp();      break;
            case 1:  turnLeft();    break;
            case 2:  turnFront();   break;
            case 3:  turnRight();   break;
            case 4:  turnDown();    break;
            case 5:  turnBack();    break;
            case 6:  returnUp();    break;
            case 7:  returnLeft();  break;
            case 8:  returnFront(); break;
            case 9:  returnRight(); break;
            case 10: returnDown();  break;
            case 11: returnBack();  break;
            default:
                throw new IllegalArgumentException("Action must be in 0..11, got " + action);
        }
    }

    /** Applies a permutation to the sticker array: {@code new[i] = old[perm[i]]}. */
    private void apply(int[] perm) {
        char[] updated = new char[stickers.length];
        for (int i = 0; i < perm.length; i++) {
            updated[i] = stickers[perm[i]];
        }
        System.arraycopy(updated, 0, stickers, 0, stickers.length);
    }

    private static int[] invert(int[] perm) {
        int[] inverse = new int[perm.length];
        for (int i = 0; i < perm.length; i++) {
            inverse[perm[i]] = i;
        }
        return inverse;
    }

    // ---------------------------------------------------------------------
    // Equality and hashing: two cubes are equal iff all stickers match. This is
    // what lets StateSet and PriorityQueueState detect duplicate configurations.
    // ---------------------------------------------------------------------

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Cube)) {
            return false;
        }
        return Arrays.equals(this.stickers, ((Cube) other).stickers);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(stickers);
    }

    /** @return a compact 54 character string of the sticker codes (UP..RIGHT). */
    public String toCompactString() {
        return new String(stickers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Face face : Face.values()) {
            sb.append(face).append(": ");
            for (int cell = 0; cell < 9; cell++) {
                sb.append(stickers[face.ordinal() * 9 + cell]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
