package place;

import java.io.Serializable;

/**
 * The Board contains a grid of Tile objects at each board coordinate. This is
 * the primary thing the clients are interested in changing.
 *
 * @author Sean Strout @ RIT CS
 */
public class PlaceTile implements Serializable {
    /** the row */
    private int row;
    /** the column */
    private int col;
    /** the color */
    private PlaceColor color;
    /** the owner */
    private String user;

    /**
     * the time the tile was changed, in milliseconds.  it is the difference,
     * measured in milliseconds, between the current time and midnight,
     * January 1, 1970 UTC.
     */
    private long time;

    /**
     * Create a tile (with no timestamp).
     *
     * @param row the row
     * @param col the column
     * @param user the username 
     * @param color the color
     */
    public PlaceTile(int row, int col, String user, PlaceColor color) {
        this(row, col, user, color, 0L);
    }

    /**
     * Create a tile (with timestamp)
     *
     * @param row the row
     * @param col the column
     * @param user the user
     * @param color the color
     * @param time current time in milliseconds
     */
    public PlaceTile(int row, int col, String user, PlaceColor color, long time) {
        this.row = row;
        this.col = col;
        this.color = color;
        this.user = user;
        this.time = time;
    }

    /**
     * Get the tile's row.
     *
     * @return the row
     */
    public int getRow() { return this.row; }

    /**
     * Get the tile's column.
     *
     * @return the column
     */
    public int getCol() { return this.col; }

    /**
     * Get the tile's color.
     *
     * @return the color
     */
    public PlaceColor getColor() {  return this.color; }

    /**
     * Get the time the tile was changed.
     *
     * @return the time
     */
    public long getTime() { return this.time; }

    /**
     * Change the tile's color
     *
     * @param color the new color
     */
    public void setColor(PlaceColor color) { this.color = color; }

    /**
     * Set the tile's timestamp.
     *
     * @param time the time the tile was last changed
     */
    public void setTime(long time) { this.time = time; }

    public String getOwner() {
        return this.user;
    }

    public void setOwner(String user) {
        this.user = user;
    }

    /**
     * Utility method for debugging only.
     *
     * @return the tile as a string
     */
    @Override
    public String toString() {
        return "Tile{" +
                "row=" + this.row +
                ", col=" + this.col +
                ", user=" + this.user +
                ", color=" + this.color +
                ", time=" + this.time +
                '}';
    }
}
