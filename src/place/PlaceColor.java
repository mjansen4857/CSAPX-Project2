package place;

import java.io.Serializable;

/**
 * Place supports breathtaking 4 bit color.  Each Tile stores one of these
 * color values.
 *
 * @author Sean Strout @ RIT CS
 */
public enum PlaceColor implements Serializable {
    BLACK("black", 0, 0, 0, 0),
    GRAY("gray", 128, 128, 128, 1),
    SILVER("silver", 192, 192, 192, 2),
    WHITE("white", 255, 255, 255, 3),
    MAROON("maroon", 128, 0, 0, 4),
    RED("red", 255, 0, 0, 5),
    OLIVE("olive", 128, 128, 0, 6),
    YELLOW("yellow", 255, 255, 0, 7),
    GREEN("green", 0, 128, 0, 8),
    LIME("lime", 0, 255, 0, 9),
    TEAL("teal", 0, 128, 128, 10),
    AQUA("aqua", 0, 255, 255, 11),
    NAVY("navy", 0, 0, 128, 12),
    BLUE("blue", 0, 0, 255, 13),
    PURPLE("purple", 128, 0, 128, 14),
    FUCHSIA("fuchsia", 255, 0, 255, 15);

    /** Yes, there are 16 colors */
    public final static int TOTAL_COLORS = 16;

    /** The color name */
    private String name;
    /** Red intensity, 0-255 */
    private int red;
    /** Green intensity, 0-255 */
    private int green;
    /** Blue intensity, 0-255 */
    private int blue;
    /** The color number, 0-15 */
    private int number;

    /**
     * Create a new color.
     *
     * @param name color name
     * @param red red intensity, 0-255
     * @param green green intensity, 0-255
     * @param blue blue intensity, 0-255
     * @param number color number, 0-15
     */
    PlaceColor(String name, int red, int green, int blue, int number) {
        this.name = name;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.number = number;
    }

    /**
     * Get the name of the color.
     *
     * @return color name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the red intensity, 0-255.
     *
     * @return red intensity
     */
    public int getRed() { return this.red; }

    /**
     * Get the green intensity, 0-255.
     *
     * @return green intensity
     */
    public int getGreen() { return this.green; }

    /**
     * Get the blue intensity, 0-255.
     *
     * @return blue intensity
     */
    public int getBlue() { return this.blue; }

    /**
     * The color number, 0-15.
     *
     * @return the color number
     */
    public int getNumber() { return this.number; }

    /**
     * Returns the hex string for the color number, 0-F.
     *
     * @return hex string value
     */
    @Override
    public String toString() {
        return Integer.toHexString(this.number).toUpperCase();
    }
}
