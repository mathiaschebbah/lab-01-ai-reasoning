package fr.dauphine.miage.ai.rubik.model;

/**
 * The six possible sticker colors of a Rubik's Cube.
 *
 * <p>Each color is identified by a single character, exactly as described in the
 * lab statement: 'W' (white), 'R' (red), 'G' (green), 'B' (blue), 'Y' (yellow)
 * and 'O' (orange).</p>
 *
 * <p>The {@link #code} is the {@code char} stored inside the {@link Cube} face
 * matrices, while {@link #display()} returns an AWT color used by the graphical
 * interface to paint the sticker.</p>
 */
public enum Color {

    WHITE('W', new java.awt.Color(0xFF, 0xFF, 0xFF)),
    RED('R', new java.awt.Color(0xB7, 0x0D, 0x0D)),
    GREEN('G', new java.awt.Color(0x00, 0x9E, 0x3D)),
    BLUE('B', new java.awt.Color(0x00, 0x51, 0xBA)),
    YELLOW('Y', new java.awt.Color(0xFF, 0xD5, 0x00)),
    ORANGE('O', new java.awt.Color(0xFF, 0x58, 0x00));

    private final char code;
    private final java.awt.Color display;

    Color(char code, java.awt.Color display) {
        this.code = code;
        this.display = display;
    }

    public char code() {
        return code;
    }

    public java.awt.Color display() {
        return display;
    }

    /** @throws IllegalArgumentException if the code is not one of W, R, G, B, Y, O. */
    public static Color fromCode(char code) {
        for (Color color : values()) {
            if (color.code == code) {
                return color;
            }
        }
        throw new IllegalArgumentException("Unknown color code: " + code);
    }
}
