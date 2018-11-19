package place;

import java.io.Serializable;

/**
 * The board is the place that holds the colored tiles.  The server creates
 * the initial Board and then transmits it only once to each client that
 * successfully logs in to the server.
 *
 * @author Sean Strout @ RIT CS
 */
public class PlaceBoard implements Serializable {
    /** The square dimension of the board */
    public final int DIM;
    /** The grid of tiles */
    private PlaceTile[][] board;

    /**
     * Create a new board of all white tiles.
     *
     * @param DIM the square dimension of the board
     */
    public PlaceBoard(int DIM) {
        this.DIM = DIM;
        this.board = new PlaceTile[DIM][DIM];
        for (int row=0; row<DIM; ++row) {
            for (int col=0; col<DIM; ++col) {
                this.board[row][col] =
                        new PlaceTile(row, col, "", PlaceColor.WHITE);
            }
        }
    }

    /**
     * Get the entire board.
     *
     * @return the board
     */
    public PlaceTile[][] getBoard() {
        return this.board;
    }

    /**
     * Get a tile on the board
     *
     * @param row row
     * @param col column
     * @rit.pre row and column constitute a valid board coordinate
     * @return the tile
     */
    public PlaceTile getTile(int row, int col){
        return this.board[row][col];
    }

    /**
     * Change a tile in the board.
     *
     * @param tile the new tile
     * @rit.pre row and column constitute a valid board coordinate
     */
    public void setTile(PlaceTile tile) {
        this.board[tile.getRow()][tile.getCol()] = tile;
    }

    /**
     * Tells whether the coordinates of the tile are valid or not
     * @param tile the tile
     * @return are the coordinates within the dimensions of the board?
     */
    public boolean isValid(PlaceTile tile) {
        return tile.getRow() >=0 &&
                tile.getRow() < this.DIM &&
                tile.getCol() >= 0 &&
                tile.getCol() < this.DIM;
    }

    /**
     * Return a string representation of the board.  It displays the tile color as
     * a single character hex value in the range 0-F.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("");
        for (int row=0; row<DIM; ++row) {
            builder.append("\n");
            for (int col=0; col<DIM; ++col) {
                builder.append(this.board[row][col].getColor());
            }
        }
        return builder.toString();
    }
}
