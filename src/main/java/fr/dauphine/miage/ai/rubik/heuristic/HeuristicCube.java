package fr.dauphine.miage.ai.rubik.heuristic;

import fr.dauphine.miage.ai.rubik.model.Cube;
import fr.dauphine.miage.ai.rubik.model.CubeGeometry;
import fr.dauphine.miage.ai.rubik.model.Face;
import fr.dauphine.miage.ai.rubik.search.State;

/**
 * Heuristic based on the piece relaxation of the Rubik's Cube problem.
 *
 * <p>The relaxation assumes that each piece moves independently of the others. A
 * piece is either a corner (three stickers) or an edge (two stickers); the six
 * centers are fixed and ignored. A piece is correctly placed when every one of
 * its stickers lies on its target face.</p>
 *
 * <p>A single rotation moves four corners and four edges at once. Therefore the
 * number of remaining moves is at least the number of misplaced corners divided
 * by four, and also at least the number of misplaced edges divided by four. The
 * heuristic returns the larger of these two admissible lower bounds:</p>
 *
 * <pre>h = max( ceil(misplacedCorners / 4), ceil(misplacedEdges / 4) )</pre>
 *
 * <p>Taking the maximum of two admissible bounds is still admissible, and this
 * piece based estimate dominates the sticker based one on most configurations.</p>
 *
 * <p>For speed, the per sticker data (cubie identifier, piece size and solved
 * color) is precomputed once into flat arrays, so {@code value} runs over the 54
 * stickers with simple array lookups and no per call allocation.</p>
 */
public final class HeuristicCube implements Heuristic {

    /** Number of corners (or edges) of a face moved by a single rotation. */
    private static final int PIECES_MOVED_PER_ROTATION = 4;

    /** Number of cubies (small cubes): 3 positions cubed. */
    private static final int CUBIE_COUNT = 27;

    /** For each sticker 0..53: the cubie it belongs to (0..26). */
    private static final int[] STICKER_CUBIE = new int[CubeGeometry.STICKER_COUNT];
    /** For each sticker 0..53: the solved color code of its face. */
    private static final char[] STICKER_SOLVED = new char[CubeGeometry.STICKER_COUNT];
    /** For each cubie 0..26: how many stickers it carries (1 center, 2 edge, 3 corner). */
    private static final int[] CUBIE_SIZE = new int[CUBIE_COUNT];

    static {
        for (Face face : Face.values()) {
            char solved = face.solvedColor().code();
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int sticker = CubeGeometry.index(face, row, col);
                    int cubie = CubeGeometry.cubieId(face, row, col);
                    STICKER_CUBIE[sticker] = cubie;
                    STICKER_SOLVED[sticker] = solved;
                    CUBIE_SIZE[cubie] = CubeGeometry.cubieStickerCount(face, row, col);
                }
            }
        }
    }

    @Override
    public int value(State state) {
        Cube cube = state.getCube();
        String stickers = cube.toCompactString();

        // misplaced[cubie] becomes true as soon as one of its stickers is off.
        boolean[] misplaced = new boolean[CUBIE_COUNT];
        for (int sticker = 0; sticker < CubeGeometry.STICKER_COUNT; sticker++) {
            if (stickers.charAt(sticker) != STICKER_SOLVED[sticker]) {
                misplaced[STICKER_CUBIE[sticker]] = true;
            }
        }

        int misplacedCorners = 0;
        int misplacedEdges = 0;
        for (int cubie = 0; cubie < CUBIE_COUNT; cubie++) {
            if (!misplaced[cubie]) {
                continue;
            }
            if (CUBIE_SIZE[cubie] == 3) {
                misplacedCorners++;
            } else if (CUBIE_SIZE[cubie] == 2) {
                misplacedEdges++;
            }
        }

        int cornerBound = ceilDiv(misplacedCorners, PIECES_MOVED_PER_ROTATION);
        int edgeBound = ceilDiv(misplacedEdges, PIECES_MOVED_PER_ROTATION);
        return Math.max(cornerBound, edgeBound);
    }

    private static int ceilDiv(int numerator, int denominator) {
        return (numerator + denominator - 1) / denominator;
    }
}
