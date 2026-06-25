package fr.dauphine.miage.ai.rubik.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds, once and for all, the sticker permutation tables of the twelve face
 * rotations from a genuine three dimensional model of the cube.
 *
 * <p>The {@link Cube} class stores the cube as six 3x3 character matrices, as
 * required by the lab statement. Deriving by hand which sticker moves where for
 * every clockwise turn (and getting every strip reversal right) is notoriously
 * error prone. Instead, this class assigns a 3D coordinate and a face normal to
 * each of the 54 stickers, rotates the relevant layer with a real 90 degree
 * rotation matrix, and reads back the resulting permutation.</p>
 *
 * <p>Each sticker is addressed by a flat index {@code face * 9 + row * 3 + col}.
 * For every face the method {@link #clockwisePermutation(Face)} returns an array
 * {@code perm} of length 54 such that, after the clockwise turn of that face,
 * {@code newStickers[i] = oldStickers[perm[i]]}. The counter clockwise turn is
 * simply the inverse permutation.</p>
 *
 * <p>This construction is deterministic and self consistent by design, so the
 * algebraic invariants checked in the tests (inverse law, order four law,
 * sticker conservation) are guaranteed to hold.</p>
 */
public final class CubeGeometry {

    /** Number of stickers on the cube (6 faces times 9 stickers). */
    public static final int STICKER_COUNT = 54;

    /**
     * Geometric description of a single sticker: the integer coordinates of the
     * cubie it sits on (each in {-1, 0, 1}) and the outward normal of the face
     * it belongs to (a unit vector along one axis).
     */
    private static final class Sticker {
        int x, y, z;        // cubie position, components in {-1, 0, 1}
        int nx, ny, nz;     // outward face normal, one component is +-1

        Sticker(int x, int y, int z, int nx, int ny, int nz) {
            this.x = x; this.y = y; this.z = z;
            this.nx = nx; this.ny = ny; this.nz = nz;
        }
    }

    private CubeGeometry() {
    }

    /**
     * Maps a (face, row, col) triple to the 3D sticker that occupies it, using
     * the viewer conventions documented in the project report. Coordinates use
     * the centered cube [-1, 1]^3; the face normal selects the outward axis.
     *
     * <p>Conventions (looking at each face from outside):</p>
     * <ul>
     *     <li>FRONT (normal +z): row+ goes down (-y), col+ goes right (+x);</li>
     *     <li>BACK  (normal -z): row+ goes down (-y), col+ goes -x;</li>
     *     <li>UP    (normal +y): row+ goes front (+z), col+ goes right (+x);</li>
     *     <li>DOWN  (normal -y): row+ goes back (-z), col+ goes right (+x);</li>
     *     <li>RIGHT (normal +x): row+ goes down (-y), col+ goes back (-z);</li>
     *     <li>LEFT  (normal -x): row+ goes down (-y), col+ goes front (+z).</li>
     * </ul>
     */
    private static Sticker stickerAt(Face face, int row, int col) {
        // a, b range over {-1, 0, 1} for the two in-face axes (col, row).
        int a = col - 1; // -1, 0, 1
        int b = row - 1; // -1, 0, 1
        switch (face) {
            case FRONT: return new Sticker(a, -b, 1, 0, 0, 1);
            case BACK:  return new Sticker(-a, -b, -1, 0, 0, -1);
            case UP:    return new Sticker(a, 1, b, 0, 1, 0);
            case DOWN:  return new Sticker(a, -1, -b, 0, -1, 0);
            case RIGHT: return new Sticker(1, -b, -a, 1, 0, 0);
            case LEFT:  return new Sticker(-1, -b, a, -1, 0, 0);
            default: throw new IllegalArgumentException("Unknown face: " + face);
        }
    }

    public static int index(Face face, int row, int col) {
        return face.ordinal() * 9 + row * 3 + col;
    }

    /**
     * Returns an identifier of the cubie (small cube) carrying the sticker at the
     * given face position. Stickers that belong to the same physical piece share
     * the same identifier, which lets the piece based heuristic group the three
     * stickers of a corner or the two stickers of an edge.
     *
     * @return a stable cubie identifier in the range 0 to 26
     */
    public static int cubieId(Face face, int row, int col) {
        Sticker s = stickerAt(face, row, col);
        // Map each coordinate from {-1, 0, 1} to {0, 1, 2} and pack into base 3.
        return (s.x + 1) * 9 + (s.y + 1) * 3 + (s.z + 1);
    }

    /**
     * Returns how many stickers sit on the cubie of the given sticker: 3 for a
     * corner, 2 for an edge, 1 for a center. Equivalent to the number of non zero
     * coordinates of the cubie position.
     */
    public static int cubieStickerCount(Face face, int row, int col) {
        Sticker s = stickerAt(face, row, col);
        int count = 0;
        if (s.x != 0) count++;
        if (s.y != 0) count++;
        if (s.z != 0) count++;
        return count;
    }

    /**
     * Computes the clockwise rotation permutation for the given face: an array
     * {@code perm} of length 54 such that, after the turn,
     * {@code newStickers[i] = oldStickers[perm[i]]}.
     */
    public static int[] clockwisePermutation(Face turnedFace) {
        // 1. Build the geometric position of every sticker.
        Sticker[] stickers = new Sticker[STICKER_COUNT];
        for (Face face : Face.values()) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    stickers[index(face, row, col)] = stickerAt(face, row, col);
                }
            }
        }

        // 2. Determine the rotation axis and which layer is affected. The layer
        //    is the set of cubies whose coordinate along the axis equals the
        //    outward sign of the turned face (the +-1 slice on that side).
        int[] normal = faceNormal(turnedFace); // {ax, ay, az}, exactly one +-1

        // 3. For each destination sticker position, find the source sticker that
        //    rotates onto it. We rotate every sticker of the affected layer by a
        //    clockwise 90 degree turn (seen from outside the turned face) and
        //    match positions + normals.
        //
        //    A clockwise turn seen from the +axis side corresponds to a rotation
        //    by -90 degrees about that axis using the right hand rule. We apply
        //    the FORWARD rotation to a source sticker to know where it lands,
        //    then invert to fill perm[destination] = source.
        int[] perm = new int[STICKER_COUNT];
        for (int i = 0; i < STICKER_COUNT; i++) {
            perm[i] = i; // stickers outside the layer stay in place by default
        }

        // Index stickers by their (position, normal) signature for fast lookup.
        for (int src = 0; src < STICKER_COUNT; src++) {
            Sticker s = stickers[src];
            if (!inLayer(s, normal)) {
                continue;
            }
            Sticker rotated = rotateClockwise(s, turnedFace);
            int dest = findSticker(stickers, rotated);
            if (dest < 0) {
                throw new IllegalStateException(
                        "Rotated sticker has no matching destination; geometry is inconsistent.");
            }
            perm[dest] = src;
        }
        return perm;
    }

    /** @return the outward normal {ax, ay, az} of a face, one component +-1. */
    private static int[] faceNormal(Face face) {
        switch (face) {
            case FRONT: return new int[] {0, 0, 1};
            case BACK:  return new int[] {0, 0, -1};
            case UP:    return new int[] {0, 1, 0};
            case DOWN:  return new int[] {0, -1, 0};
            case RIGHT: return new int[] {1, 0, 0};
            case LEFT:  return new int[] {-1, 0, 0};
            default: throw new IllegalArgumentException("Unknown face: " + face);
        }
    }

    /** @return true if the sticker's cubie lies on the +-1 slice of the axis. */
    private static boolean inLayer(Sticker s, int[] normal) {
        if (normal[0] != 0) return s.x == normal[0];
        if (normal[1] != 0) return s.y == normal[1];
        return s.z == normal[2];
    }

    /**
     * Rotates a sticker (its position and its normal) by a clockwise quarter
     * turn as seen by a viewer outside the given face.
     *
     * <p>Looking at a face from outside along its outward normal, a clockwise
     * turn maps the two in-plane axes (u, v) by {@code (u, v) -> (v, -u)} where
     * (u, v, n) is a right handed frame with n the outward normal.</p>
     */
    private static Sticker rotateClockwise(Sticker s, Face face) {
        int x = s.x, y = s.y, z = s.z;
        int nx = s.nx, ny = s.ny, nz = s.nz;
        switch (face) {
            // Outward normal +z. Right handed in-plane frame (u=+x, v=+y).
            // Clockwise seen from +z: (x, y) -> (y, -x).
            case FRONT: return rebuild(y, -x, z, ny, -nx, nz);
            // Outward normal -z. Viewer on -z side. Frame (u=+x, v=-y).
            // Clockwise: rotate by +90 about z in world terms.
            case BACK:  return rebuild(-y, x, z, -ny, nx, nz);
            // Outward normal +y. Looking down from +y, a clockwise turn maps the
            // in-plane axes (x, z) by (x, z) -> (-z, x), so x' = -z and z' = x.
            case UP:    return rebuild(-z, y, x, -nz, ny, nx);
            // Outward normal -y. Looking up from -y, the clockwise sense is the
            // mirror of UP: (x, z) -> (z, -x).
            case DOWN:  return rebuild(z, y, -x, nz, ny, -nx);
            // Outward normal +x. Frame (u=+y, v=+z) right handed (y cross z = x).
            // Clockwise seen from +x: (y, z) -> (z, -y).
            case RIGHT: return rebuild(x, z, -y, nx, nz, -ny);
            // Outward normal -x. Viewer on -x side. Opposite sense.
            case LEFT:  return rebuild(x, -z, y, nx, -nz, ny);
            default: throw new IllegalArgumentException("Unknown face: " + face);
        }
    }

    private static Sticker rebuild(int x, int y, int z, int nx, int ny, int nz) {
        return new Sticker(x, y, z, nx, ny, nz);
    }

    /** @return the index of the sticker matching the given position and normal. */
    private static int findSticker(Sticker[] stickers, Sticker target) {
        for (int i = 0; i < stickers.length; i++) {
            Sticker s = stickers[i];
            if (s.x == target.x && s.y == target.y && s.z == target.z
                    && s.nx == target.nx && s.ny == target.ny && s.nz == target.nz) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the list of disjoint cycles of a permutation, for documentation
     * and debugging. Each cycle is a list of flat sticker indices. Only the non
     * trivial cycles (length greater than one) are returned.
     */
    public static List<List<Integer>> cycles(int[] perm) {
        boolean[] seen = new boolean[perm.length];
        List<List<Integer>> result = new ArrayList<>();
        for (int start = 0; start < perm.length; start++) {
            if (seen[start] || perm[start] == start) {
                seen[start] = true;
                continue;
            }
            List<Integer> cycle = new ArrayList<>();
            int current = start;
            while (!seen[current]) {
                seen[current] = true;
                cycle.add(current);
                current = perm[current];
            }
            if (cycle.size() > 1) {
                result.add(cycle);
            }
        }
        return result;
    }
}
