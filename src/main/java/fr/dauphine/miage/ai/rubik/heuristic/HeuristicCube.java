package fr.dauphine.miage.ai.rubik.heuristic;

import fr.dauphine.miage.ai.rubik.model.Color;
import fr.dauphine.miage.ai.rubik.model.Cube;
import fr.dauphine.miage.ai.rubik.model.CubeGeometry;
import fr.dauphine.miage.ai.rubik.model.Face;
import fr.dauphine.miage.ai.rubik.search.State;
import java.util.HashMap;
import java.util.Map;

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
 */
public final class HeuristicCube implements Heuristic {

    /** Number of corners (or edges) of a face moved by a single rotation. */
    private static final int PIECES_MOVED_PER_ROTATION = 4;

    @Override
    public int value(State state) {
        Cube cube = state.getCube();

        // For each cubie, remember whether all of its stickers are well placed
        // and whether it is a corner (3 stickers) or an edge (2 stickers).
        Map<Integer, Boolean> wellPlaced = new HashMap<>();
        Map<Integer, Integer> stickerCount = new HashMap<>();

        for (Face face : Face.values()) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int cubie = CubeGeometry.cubieId(face, row, col);
                    int count = CubeGeometry.cubieStickerCount(face, row, col);
                    stickerCount.put(cubie, count);

                    Color color = Color.fromCode(cube.get(face, row, col));
                    boolean stickerOk = (face.solvedColor() == color);
                    wellPlaced.merge(cubie, stickerOk, Boolean::logicalAnd);
                }
            }
        }

        int misplacedCorners = 0;
        int misplacedEdges = 0;
        for (Map.Entry<Integer, Integer> entry : stickerCount.entrySet()) {
            int cubie = entry.getKey();
            int count = entry.getValue();
            if (count == 1) {
                continue; // Center: fixed, never counted.
            }
            if (Boolean.TRUE.equals(wellPlaced.get(cubie))) {
                continue; // Piece fully in place.
            }
            if (count == 3) {
                misplacedCorners++;
            } else if (count == 2) {
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
