package place.client.model;

import place.PlaceBoard;
import place.PlaceTile;
import java.util.Observable;

/**
 * TODO
 * @author Michael Jansen
 * @author Tyson Levy
 * @author Leelan Carbonell
 */
public class ClientModel extends Observable {

    private PlaceBoard board;

    private boolean running = true;

    private boolean ready;

    private PlaceTile lastTileChanged;

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
        this.lastTileChanged = tile;
        super.setChanged();
        super.notifyObservers();
    }

    /**
     * TODO
     * @param game
     */
    public void initBoard(PlaceBoard game){
        board = game;
        this.ready = true;
        super.setChanged();
        super.notifyObservers();
    }

    /**
     * TODO
     * @return
     */
    public boolean isRunning(){
        return this.running;
    }

    /**
     * TODO
     */
    public void close(){
        this.running = false;
    }

    /**
     * TODO
     * @return
     */
    public int getDim(){ return board.DIM; }

    /**
     * TODO
     * @return
     */
    public boolean isReady(){return ready; }

    /**
     * TODO
     * @return
     */
    public PlaceTile getLastTileChanged(){return lastTileChanged;}

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
