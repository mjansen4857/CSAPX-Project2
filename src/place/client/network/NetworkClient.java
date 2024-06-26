package place.client.network;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.client.model.ClientModel;
import place.network.PlaceRequest;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.Scanner;


/**
 * The client side network interface to a Place server.
 * Each client gets its own connection to the server.
 * This class represents the controller part of a model-view-controller
 * triumvirate, in that part of its purpose is to forward user actions
 * to the remote server.
 *
 * @author Robert St Jacques @ RIT SE
 * @author Sean Strout @ RIT CS
 * @author James Heliotis @ RIT CS
 * @author Tyson Levy
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
     * The {@link Socket} used to communicate with the place server.
     */
    private Socket sock;

    /**
     * The {@link Scanner} used to read requests from the place server.
     */
    private ObjectInputStream networkIn;

    /**
     * The {@link PrintStream} used to write responses to the okace server.
     */
    private ObjectOutputStream networkOut;

    /**
     * The {@link ClientModel} used to keep track of the state of the game.
     */
    public ClientModel game;

    /**
     * Sentinel used to control the main game loop.
     */
    private boolean go;
    /**
     * Username for the connection. Used when sending moves.
     */
    private String username;

    private boolean loaded = false;
    private long lastSendTime = 0;

    public synchronized boolean isLoaded(){return loaded;}

    /**
     * Accessor that takes multithreaded access into account
     *
     * @return whether it ok to continue or not
     */
    public synchronized boolean goodToGo() {
        return this.go;
    }

    /**
     * Multithread-safe mutator
     */
    private synchronized void stop() {
        this.go = false;
    }

    /**
     * Hook up with a Place game server already running and waiting for
     * clients to connect.
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
            this.sock = new Socket( hostname, port );
            this.networkOut = new ObjectOutputStream( sock.getOutputStream() );
            this.networkIn = new ObjectInputStream( sock.getInputStream() );
            this.game = model;
            this.username = username;
            this.go = true;

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
     * returns the username for the client
     */
    public String getUsername(){
        return this.username;
    }

    /**
     * Sends a move to the server if its been over 500 milliseconds since the last change
     * @param row
     * @param col
     * @param color
     */
    public void sendMove(int row, int col, PlaceColor color) {
        if(System.currentTimeMillis() - lastSendTime >= 500) {
            lastSendTime = System.currentTimeMillis();
            try {
                if (row != -1) {
                    networkOut.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.CHANGE_TILE, new PlaceTile(row, col, username, color, System.currentTimeMillis())));
                } else {
                    this.close();
                }
            }catch (SocketException e){}
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method should be called at the end of the game to
     * close the client connection.
     */
    public void close() {
        try {

            this.go = false;
            this.networkIn.close();
            this.networkOut.close();
            this.sock.close();
        }
        catch (SocketException e) {}
        catch( IOException ioe ) {
            // squash
            ioe.printStackTrace();
        }
        this.game.close();
    }

    /**
     * Run the main client loop. Intended to be started as a separate
     * thread internally. This method is made private so that no one
     * outside will call it or try to start a thread on it.
     */
    private void run() {

        try {
            this.networkOut.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, this.username));
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }

        while(this.goodToGo()) try {
            PlaceRequest<?> request = (PlaceRequest<?>) networkIn.readUnshared();
            if (request.getType() == PlaceRequest.RequestType.LOGIN_SUCCESS) {
                System.out.println("Login Success");
            } else if (request.getType() == PlaceRequest.RequestType.ERROR) {
                System.err.println((String) request.getData());
            } else if (request.getType() == PlaceRequest.RequestType.BOARD) {
                game.initBoard((PlaceBoard) request.getData());
                System.out.println("Board received:");
                this.loaded = true;
            } else if (request.getType() == PlaceRequest.RequestType.TILE_CHANGED) {
                //System.out.println("\nTile Changed: " + request.getData());
                game.setTile((PlaceTile) request.getData());
            }
        }catch (SocketException e) {}
        catch (EOFException e) {
            close();
            System.out.println("SERVER CLOSED");
        } catch (NoSuchElementException nse) {
            // Looks like the connection shut down.
            nse.printStackTrace();
            this.stop();
        } catch (Exception e) {
            e.printStackTrace();
            this.stop();
        }
        this.close();
    }

}
