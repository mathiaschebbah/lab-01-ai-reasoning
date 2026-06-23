package fr.dauphine.miage.ai.rubik.model;

/**
 * The six faces of the cube.
 *
 * <p>Each face is bound to the color it must show in the solved configuration,
 * following the Western color scheme used throughout this project:</p>
 *
 * <ul>
 *     <li>{@code UP} is white,</li>
 *     <li>{@code DOWN} is yellow,</li>
 *     <li>{@code FRONT} is red,</li>
 *     <li>{@code BACK} is orange,</li>
 *     <li>{@code LEFT} is blue,</li>
 *     <li>{@code RIGHT} is green.</li>
 * </ul>
 *
 * <p>Opposite faces are UP/DOWN, FRONT/BACK and LEFT/RIGHT.</p>
 */
public enum Face {

    UP(Color.WHITE),
    DOWN(Color.YELLOW),
    FRONT(Color.RED),
    BACK(Color.ORANGE),
    LEFT(Color.BLUE),
    RIGHT(Color.GREEN);

    private final Color solvedColor;

    Face(Color solvedColor) {
        this.solvedColor = solvedColor;
    }

    /** @return the color this face must be uniformly filled with when solved. */
    public Color solvedColor() {
        return solvedColor;
    }
}
