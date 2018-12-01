package place.client.model;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;

import java.util.Observable;

public class ClientModel extends Observable {

    private PlaceBoard board;

    private boolean running = true;

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
    public PlaceBoard getBoard() {
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
        return this.board.getTile(row, col);
    }

    /**
     * Change a tile in the board.
     *
     * @param tile the new tile
     * @rit.pre row and column constitute a valid board coordinate
     */
    public void setTile(PlaceTile tile) {
        this.board.setTile(tile);
        super.setChanged();
        super.notifyObservers();
    }

    public void initBoard(PlaceBoard game){
        board = game;
        super.setChanged();
        super.notifyObservers();
    }

    public boolean isRunning(){
        return this.running;
    }

    public void close(){
        this.running = false;
    }

    public int getDim(){
        return board.DIM;
    }

    /**
     * Return a string representation of the board.  It displays the tile color as
     * a single character hex value in the range 0-F.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return board.toString();
    }
}
