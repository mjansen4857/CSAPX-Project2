package place.client.model;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;

import java.util.Observable;

public class ClientModel extends Observable {

    private int DIM;
    private PlaceTile[][] board;

    /**
     * Create a new board of all white tiles.
     */
    public ClientModel() {
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

    public void allocate(int DIM){
        this.DIM = DIM;
        board = new PlaceTile[DIM][DIM];
    }

    public void initBoard(PlaceBoard game){
        for(int row=0; row < game.getBoard().length; row++){
            for(int col=0; row < game.getBoard()[row].length; col++){
                board[row][col] = game.getBoard()[row][col];
            }
        }
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
