package fr.dauphine.miage.ai.rubik.heuristic;

import fr.dauphine.miage.ai.rubik.model.Color;
import fr.dauphine.miage.ai.rubik.model.Cube;
import fr.dauphine.miage.ai.rubik.model.CubeGeometry;
import fr.dauphine.miage.ai.rubik.model.Face;
import fr.dauphine.miage.ai.rubik.search.State;

/**
 * Heuristic based on the sticker relaxation of the Rubik's Cube problem.
 *
 * <p>The relaxation assumes that each sticker can move on its own, independently
 * of the others. Under this assumption a sticker only needs to reach its correct
 * face, and the cost to do so is the 3D Manhattan distance between its current
 * face and its target face, measured in quarter turns:</p>
 *
 * <ul>
 *     <li>0 if the sticker is already on its correct face,</li>
 *     <li>1 if its face is adjacent to the target face,</li>
 *     <li>2 if its face is opposite to the target face.</li>
 * </ul>
 *
 * <p>The heuristic value is the sum of those distances over all 54 stickers,
 * divided by the number of stickers that can change face in a single rotation,
 * which is twelve (the belt of three stickers on each of the four faces around
 * the turned face). Dividing by twelve makes the estimate admissible: one
 * rotation moves twelve belt stickers, each to an adjacent face, so each can cut
 * its face distance by at most one; the remaining move count is therefore at
 * least the total distance divided by twelve.</p>
 *
 * <p>For speed, a small distance table indexed by (current face, sticker color)
 * is precomputed once, together with the face of each of the 54 sticker
 * positions, so {@code value} is a plain scan with no per call allocation.</p>
 */
public final class HeuristicLabel implements Heuristic {

    /** The number of stickers that change face in a single rotation. */
    private static final int STICKERS_MOVED_PER_ROTATION = 12;

    /** Face that owns each sticker position 0..53. */
    private static final Face[] STICKER_FACE = new Face[CubeGeometry.STICKER_COUNT];

    /** DISTANCE[currentFace ordinal][color ordinal] in {0, 1, 2}. */
    private static final int[][] DISTANCE = new int[6][6];

    static {
        for (Face face : Face.values()) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    STICKER_FACE[CubeGeometry.index(face, row, col)] = face;
                }
            }
        }
        for (Face from : Face.values()) {
            for (Color color : Color.values()) {
                Face target = targetFace(color);
                DISTANCE[from.ordinal()][color.ordinal()] = faceDistance(from, target);
            }
        }
    }

    @Override
    public int value(State state) {
        Cube cube = state.getCube();
        String stickers = cube.toCompactString();
        int totalDistance = 0;
        for (int sticker = 0; sticker < CubeGeometry.STICKER_COUNT; sticker++) {
            Face from = STICKER_FACE[sticker];
            int colorOrdinal = Color.fromCode(stickers.charAt(sticker)).ordinal();
            totalDistance += DISTANCE[from.ordinal()][colorOrdinal];
        }
        return totalDistance / STICKERS_MOVED_PER_ROTATION;
    }

    /** @return the face on which a sticker of the given color belongs when solved. */
    private static Face targetFace(Color color) {
        for (Face face : Face.values()) {
            if (face.solvedColor() == color) {
                return face;
            }
        }
        throw new IllegalStateException("No solved face for color " + color);
    }

    /**
     * Returns the 3D Manhattan distance between two faces, measured as the number
     * of quarter turns needed to bring a sticker from one face to the other: 0
     * for the same face, 1 for adjacent faces, 2 for opposite faces.
     *
     * @param from the current face
     * @param to   the target face
     * @return the face distance in {0, 1, 2}
     */
    static int faceDistance(Face from, Face to) {
        if (from == to) {
            return 0;
        }
        return areOpposite(from, to) ? 2 : 1;
    }

    /** @return whether two faces are opposite (UP/DOWN, FRONT/BACK, LEFT/RIGHT). */
    private static boolean areOpposite(Face a, Face b) {
        return (a == Face.UP && b == Face.DOWN) || (a == Face.DOWN && b == Face.UP)
                || (a == Face.FRONT && b == Face.BACK) || (a == Face.BACK && b == Face.FRONT)
                || (a == Face.LEFT && b == Face.RIGHT) || (a == Face.RIGHT && b == Face.LEFT);
    }
}
