package fr.dauphine.miage.ai.rubik.ui;

import fr.dauphine.miage.ai.rubik.model.Color;
import fr.dauphine.miage.ai.rubik.model.Cube;
import fr.dauphine.miage.ai.rubik.model.Face;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * A Swing panel that draws a cube as an unfolded net (a cross layout).
 *
 * <p>The six faces are laid out as:</p>
 *
 * <pre>
 *            [ UP ]
 *     [LEFT][FRONT][RIGHT][BACK]
 *            [DOWN]
 * </pre>
 *
 * <p>Each face is a 3x3 grid of colored stickers painted from the {@link Cube}
 * model, so the panel always reflects the current configuration.</p>
 */
public final class CubePanel extends JPanel {

    /** Pixel size of one sticker. */
    private static final int STICKER = 34;
    /** Gap in pixels between faces. */
    private static final int FACE_GAP = 8;
    /** Outer margin in pixels. */
    private static final int MARGIN = 16;

    private Cube cube;

    public CubePanel(Cube cube) {
        this.cube = cube;
        int faceSize = 3 * STICKER;
        int width = MARGIN * 2 + 4 * faceSize + 3 * FACE_GAP;
        int height = MARGIN * 2 + 3 * faceSize + 2 * FACE_GAP;
        setPreferredSize(new Dimension(width, height));
        setBackground(new java.awt.Color(0x2B, 0x2B, 0x2B));
    }

    /** Updates the cube shown by the panel and repaints. */
    public void setCube(Cube cube) {
        this.cube = cube;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int faceSize = 3 * STICKER;
        int col1 = MARGIN;                                   // LEFT column
        int col2 = col1 + faceSize + FACE_GAP;               // FRONT / UP / DOWN column
        int col3 = col2 + faceSize + FACE_GAP;               // RIGHT column
        int col4 = col3 + faceSize + FACE_GAP;               // BACK column
        int row1 = MARGIN;                                   // UP row
        int row2 = row1 + faceSize + FACE_GAP;               // middle row
        int row3 = row2 + faceSize + FACE_GAP;               // DOWN row

        drawFace(g2, Face.UP, col2, row1);
        drawFace(g2, Face.LEFT, col1, row2);
        drawFace(g2, Face.FRONT, col2, row2);
        drawFace(g2, Face.RIGHT, col3, row2);
        drawFace(g2, Face.BACK, col4, row2);
        drawFace(g2, Face.DOWN, col2, row3);

        // Face labels above each face block.
        g2.setColor(java.awt.Color.LIGHT_GRAY);
        g2.drawString("U", col2, row1 - 3);
        g2.drawString("L", col1, row2 - 3);
        g2.drawString("F", col2, row2 - 3);
        g2.drawString("R", col3, row2 - 3);
        g2.drawString("B", col4, row2 - 3);
        g2.drawString("D", col2, row3 - 3);
    }

    private void drawFace(Graphics2D g2, Face face, int originX, int originY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Color color = Color.fromCode(cube.get(face, row, col));
                int x = originX + col * STICKER;
                int y = originY + row * STICKER;
                g2.setColor(color.display());
                g2.fillRoundRect(x + 1, y + 1, STICKER - 2, STICKER - 2, 6, 6);
                g2.setColor(java.awt.Color.DARK_GRAY);
                g2.drawRoundRect(x + 1, y + 1, STICKER - 2, STICKER - 2, 6, 6);
            }
        }
    }
}
