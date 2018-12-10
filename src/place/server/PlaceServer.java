package place.server;

import place.PlaceBoard;
import place.PlaceTile;
import place.network.PlaceRequest;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Implementation of a Place Server. This is a multi-threaded server that
 * spawns a new thread to handle every client that connects
 *
 * @author Michael Jansen
 * @author Tyson Levy
 * @author Leelan Carbonell
 */
public class PlaceServer{
    public static PlaceServer instance;
    private ServerSocket server;
    protected PlaceBoard board;
    private volatile boolean running = true;
    private HashMap<String, ClientThread> clients;
    private InetAddress lastConnect = null;
    private long lastConnectTime = 0;
    private ServerStatistics serverStatistics;
    protected long startTime;
    protected long endTime;

    /**
     * Constructs a place server. Starts running a server socket
     * and prepares everything
     * @param port The port to run the server on
     * @param dim The dimension of the place board
     */
    public PlaceServer(int port, int dim){
        try {
            this.server = new ServerSocket(port);
            this.startTime = System.currentTimeMillis();
            this.board = new PlaceBoard(dim);
            this.clients = new HashMap<>();
            this.serverStatistics = new ServerStatistics(this);
            instance = this;
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Updates a tile on the place board and then sends that update to all connected clients
     * @param tile The tile that should be updated
     * @throws IOException
     */
    public synchronized void updateTile(PlaceTile tile) throws IOException{
        synchronized (board) {
            if (board.isValid(tile)) {
                board.setTile(tile);
                serverStatistics.changeTile(tile);
                for (ClientThread client : clients.values()) {
                    client.sendMessage(new PlaceRequest<>(PlaceRequest.RequestType.TILE_CHANGED, tile));
                }
            }
        }
    }

    /**
     * Adds a client to the list of connected clients and sends them the current state of
     * the board
     * @param username The username of the client
     * @param thread The thread that the client is running on
     * @throws IOException
     */
    public void addClient(String username, ClientThread thread) throws IOException{
        clients.put(username, thread);
        thread.sendMessage(new PlaceRequest<>(PlaceRequest.RequestType.BOARD, board));
    }

    /**
     * Stop the server cleanly if the user types STOP
     */
    public void check(){
        Scanner in = new Scanner(System.in);
        while(running){
            if(in.nextLine().equals("STOP")){
                System.out.println("SERVER CLOSING");
                running = false;
                for(ClientThread client:clients.values()){
                    client.closeAll();
                }
                try {
                    server.close();
                    this.endTime = System.currentTimeMillis();
                    this.serverStatistics.generateReport();
                    System.exit(0);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * The main loop of the server. This loop waits for a connection to the server then spawns
     * a thread to handle that client
     */
    public void runServer(){
        while (running){
            try {
                System.out.println("Waiting for connection...");
                Socket socket = server.accept();
                System.out.println("Incoming connection from " + socket);
                if(lastConnect != null){
                    if(lastConnect.equals(socket.getInetAddress()) && System.currentTimeMillis() - lastConnectTime < 100){
                        System.out.println("Prevented connection from " + socket + " for too many connections from that address in a row");
                        socket.close();
                        continue;
                    }
                }
                lastConnect = socket.getInetAddress();
                lastConnectTime = System.currentTimeMillis();
                new Thread(new ClientThread(socket)).start();
            }catch (IOException e){
                //e.printStackTrace();
            }
        }
    }

    /**
     * This subclass is a thread used to handle communication with a client
     */
    private class ClientThread implements Runnable{
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String username = "";
        private long lastChangeTime = 0;

        /**
         * Construct the client thread
         * @param socket The socket representing the connection to a client
         */
        public ClientThread(Socket socket){
            this.socket = socket;
            try {
                this.in = new ObjectInputStream(this.socket.getInputStream());
                this.out = new ObjectOutputStream(this.socket.getOutputStream());

            }catch (IOException e){
                e.printStackTrace();
                closeAll();
            }
        }

        /**
         * Send a message to the client
         * @param request The request to send
         * @throws IOException
         */
        public void sendMessage(PlaceRequest request) throws IOException{
            out.writeUnshared(request);
            out.flush();
        }

        /**
         * Handle a message received from the client
         * @param request The request from the client
         * @throws IOException
         */
        public void handleMessage(PlaceRequest request) throws IOException{
            if(request.getType() == PlaceRequest.RequestType.LOGIN){
                this.username = (String) request.getData();
                if(clients.containsKey(username)){
                    System.out.println("Username already exists: " + username);
                    sendMessage(new PlaceRequest<>(PlaceRequest.RequestType.ERROR, "Username already taken!"));
                }else{
                    sendMessage(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN_SUCCESS, username));
                    PlaceServer.instance.addClient(username, this);
                    System.out.println("User: " + username + " connected");

                }
            }else if(request.getType() == PlaceRequest.RequestType.CHANGE_TILE){
                if(System.currentTimeMillis() - lastChangeTime >= 500) {
                    PlaceTile tile = (PlaceTile) request.getData();
                    PlaceServer.instance.updateTile(tile);
                    lastChangeTime = System.currentTimeMillis();
                }
            }
        }

        /**
         * Close the socket and input/output streams
         */
        public void closeAll(){
            try {
                if(socket != null) socket.close();
                if(in != null) in.close();
                if(out != null) out.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run(){
            if(socket == null) return;
            try{
                while (socket.isConnected() && running){
                    PlaceRequest<?> request = (PlaceRequest<?>) in.readUnshared();
                    Thread handle = new Thread(() -> {
                        try {
                            handleMessage(request);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    handle.start();
                    Thread.sleep(10);
                }
            }
            catch (SocketException e){}
            catch (EOFException e){}
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                System.out.println("User: " + username + " disconnected");
                clients.remove(username);
                closeAll();
            }
        }
    }

    public static void main(String[] args) {
        if(args.length != 2){
            System.err.println("Usage: java PlaceServer port DIM");
            System.exit(-1);
        }

        int port = Integer.parseInt(args[0]);
        int dim = Integer.parseInt(args[1]);

        PlaceServer placeServer = new PlaceServer(port, dim);
        Thread checking = new Thread(() -> placeServer.check());
        checking.start();
        placeServer.runServer();
    }
}
