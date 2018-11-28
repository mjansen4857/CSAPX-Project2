package place.client.network;

import place.PlaceException;
import place.client.model.ClientModel;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private ObjectInputStream networkIn;

    /**
     * The {@link PrintStream} used to write responses to the reversi server.
     */
    private ObjectOutputStream networkOut;

    /**
     * The {@link ClientModel} used to keep track of the state of the game.
     */
    private ClientModel game;

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
    public NetworkClient( String hostname, int port, String username, ClientModel model )
            throws PlaceException {

        try {
            System.out.println("TEST1");
            this.sock = new Socket( hostname, port );
            System.out.println("TEST2");
            this.networkIn = new ObjectInputStream( sock.getInputStream() );
            System.out.println("TEST3");
            this.networkOut = new ObjectOutputStream( sock.getOutputStream() );
            System.out.println("TEST4");
            this.game = model;
            System.out.println("TEST5");
            this.go = true;
            System.out.println("TEST6");

            this.networkOut.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, username));
            this.networkOut.flush();
            // Block waiting for the CONNECT message from the server.
            PlaceRequest<?> request = (PlaceRequest<?>) this.networkIn.readUnshared();

            if (request.getType() == PlaceRequest.RequestType.LOGIN_SUCCESS){
                System.out.println("Login success");
            }
            else if (request.getType() == PlaceRequest.RequestType.ERROR){
                System.err.println((String) request.getData());
                System.exit(-1);
            }

            // Run rest of client in separate thread.
            // This threads stops on its own at the end of the game and
            // does not need to rendez-vous with other software components.
            Thread netThread = new Thread( () -> this.run() );
            netThread.start();
        }
        catch( IOException e ) {
            throw new PlaceException( e );
        }
        catch( ClassNotFoundException e ){
            System.err.println(e);
            System.exit(-1);
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
     * Called when the server sends a message saying that
     * gameplay is damaged. Ends the game.
     *
     * @param arguments The error message sent from the reversi.server.
     */
    public void error( String arguments ) {
        NetworkClient.dPrint( '!' + PlaceRequest.RequestType.ERROR.toString() + ',' + arguments );
        dPrint( "Fatal error: " + arguments );
        //this.game.error( arguments );
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
        //this.game.close();
    }

    /**
     * Run the main client loop. Intended to be started as a separate
     * thread internally. This method is made private so that no one
     * outside will call it or try to start a thread on it.
     */
    private void run() {

        while ( this.goodToGo() ) {
            try {

                PlaceRequest<?> request = (PlaceRequest<?>) this.networkIn.readUnshared();
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
