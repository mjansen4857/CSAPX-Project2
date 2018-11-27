package place;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;


/**
 * The client side network interface to a Reversi game server.
 * Each of the two players in a game gets its own connection to the server.
 * This class represents the controller part of a model-view-controller
 * triumvirate, in that part of its purpose is to forward user actions
 * to the remote server.
 *
 * @author Robert St Jacques @ RIT SE
 * @author Sean Strout @ RIT CS
 * @author James Heliotis @ RIT CS
 */
public class NetworkClient {

    /**
     * Turn on if standard output debug messages are desired.
     */
    private static final boolean DEBUG = false;

    /**
     * Print method that does something only if DEBUG is true
     *
     * @param logMsg the message to log
     */
    private static void dPrint( Object logMsg ) {
        if ( NetworkClient.DEBUG ) {
            System.out.println( logMsg );
        }
    }

    /**
     * The {@link Socket} used to communicate with the reversi server.
     */
    private Socket sock;

    /**
     * The {@link Scanner} used to read requests from the reversi server.
     */
    private Scanner networkIn;

    /**
     * The {@link PrintStream} used to write responses to the reversi server.
     */
    private PrintStream networkOut;

    /**
     * The {@link Board} used to keep track of the state of the game.
     */
    private Board game;

    /**
     * Sentinel used to control the main game loop.
     */
    private boolean go;

    /**
     * Accessor that takes multithreaded access into account
     *
     * @return whether it ok to continue or not
     */
    private synchronized boolean goodToGo() {
        return this.go;
    }

    /**
     * Multithread-safe mutator
     */
    private synchronized void stop() {
        this.go = false;
    }

    /**
     * Hook up with a Reversi game server already running and waiting for
     * two players to connect. Because of the nature of the server
     * protocol, this constructor actually blocks waiting for the first
     * message from the server that tells it how big the board will be.
     * Afterwards a thread that listens for server messages and forwards
     * them to the game object is started.
     *
     * @param hostname the name of the host running the server program
     * @param port     the port of the server socket on which the server is
     *                 listening
     * @param model    the local object holding the state of the game that
     *                 must be updated upon receiving server messages
     * @throws PlaceException If there is a problem opening the connection
     */
    public NetworkClient( String hostname, int port, Board model )
            throws PlaceException {
        try {
            this.sock = new Socket( hostname, port );
            this.networkIn = new Scanner( sock.getInputStream() );
            this.networkOut = new PrintStream( sock.getOutputStream() );
            this.game = model;
            this.go = true;

            // Block waiting for the CONNECT message from the server.
            String request = this.networkIn.next();
            String arguments = this.networkIn.nextLine();
            assert request.equals( ReversiProtocol.CONNECT ) :
                    "CONNECT not 1st";
            NetworkClient.dPrint( "Connected to server " + this.sock );
            this.connect( arguments );

            // Run rest of client in separate thread.
            // This threads stops on its own at the end of the game and
            // does not need to rendez-vous with other software components.
            Thread netThread = new Thread( () -> this.run() );
            netThread.start();
        }
        catch( IOException e ) {
            throw new PlaceException( e );
        }
    }


    /**
     * Called by the constructor to set up the game board for this player now
     * that the server has sent the board dimensions with the
     *
     *
     * @param arguments string from the server's message that
     *                  contains the square dimension of the board
     * @throws PlaceException if the dimensions be small
     */
    public void connect( String arguments ) throws PlaceException {
        // Get numbers from string
        String fields[] = arguments.trim().split( " " );
        int DIM = Integer.parseInt( fields[ 0 ] );

        // Get the board state set up.
        this.game.allocate( DIM ); // may throw exception
    }

    /**
     * Tell the local user to choose a move. How this is communicated to
     * the user is up to the View (UI).
     */
    private void makeMove() {
        this.game.makeMove();
    }

    /**
     * A move has been made by one of the players
     *
     * @param arguments string from the server's message that
     *                  contains the row, then column where the
     *                  player made the move
     */
    public void moveMade( String arguments ) {
        NetworkClient.dPrint( '!' + ',' + arguments );

        String[] fields = arguments.trim().split( " " );
        int row = Integer.parseInt( fields[ 0 ] );
        int column = Integer.parseInt( fields[ 1 ] );

        // Update the board model.
        this.game.moveMade( row, column );
    }

    /**
     * Called when the server sends a message saying that the
     * game has been won by this player. Ends the game.
     */
    public void gameWon() {
        NetworkClient.dPrint( '!' + GAME_WON );

        dPrint( "You won! Yay!" );
        this.game.gameWon();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that the
     * game has been won by the other player. Ends the game.
     */
    public void gameLost() {
        NetworkClient.dPrint( '!' + GAME_LOST );
        dPrint( "You lost! Boo!" );
        this.game.gameLost();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that the
     * game is a tie. Ends the game.
     */
    public void gameTied() {
        NetworkClient.dPrint( '!' + GAME_TIED );
        dPrint( "You tied! Meh!" );
        this.game.gameTied();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that
     * gameplay is damaged. Ends the game.
     *
     * @param arguments The error message sent from the reversi.server.
     */
    public void error( String arguments ) {
        NetworkClient.dPrint( '!' + ERROR + ',' + arguments );
        dPrint( "Fatal error: " + arguments );
        this.game.error( arguments );
        this.stop();
    }

    /**
     * This method should be called at the end of the game to
     * close the client connection.
     */
    public void close() {
        try {
            this.sock.close();
        }
        catch( IOException ioe ) {
            // squash
        }
        this.game.close();
    }

    /**
     * UI wants to send a new move to the server.
     *
     * @param row the row
     * @param col the column
     */
    public void sendMove( int row, int col ) {
        this.networkOut.println( MOVE + " " + row + " " + col );
    }

    /**
     * Run the main client loop. Intended to be started as a separate
     * thread internally. This method is made private so that no one
     * outside will call it or try to start a thread on it.
     */
    private void run() {

        while ( this.goodToGo() ) {
            try {
                String request = this.networkIn.next();
                String arguments = this.networkIn.nextLine().trim();
                NetworkClient.dPrint( "Net message in = \"" + request + '"' );

            }
            catch( NoSuchElementException nse ) {
                // Looks like the connection shut down.
                this.error( "Lost connection to server." );
                this.stop();
            }
            catch( Exception e ) {
                this.error( e.getMessage() + '?' );
                this.stop();
            }
        }
        this.close();
    }

}
