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

import java.util.HashMap;
import java.util.Scanner;

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

    public synchronized void updateTile(PlaceTile tile) throws IOException{
        if(board.isValid(tile)){
            board.setTile(tile);
            serverStatistics.changeTile(tile);
            for(ClientThread client:clients.values()){
                client.sendMessage(new PlaceRequest<>(PlaceRequest.RequestType.TILE_CHANGED, tile));
            }
        }
    }

    public void addClient(String username, ClientThread thread) throws IOException{
        clients.put(username, thread);
        thread.sendMessage(new PlaceRequest<>(PlaceRequest.RequestType.BOARD, board));
    }

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
                catch (IOException e){}
            }
        }
    }
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

    private class ClientThread implements Runnable{
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String username = "";
        private long lastChangeTime = 0;

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

        public void sendMessage(PlaceRequest request) throws IOException{
            out.writeUnshared(request);
            out.flush();
        }

        public void handleMessage(PlaceRequest request) throws IOException{
            if(request.getType() == PlaceRequest.RequestType.LOGIN){
                this.username = (String) request.getData();
                if(clients.containsKey(username)){
                    System.out.println("Username already exists: " + username);
                    sendMessage(new PlaceRequest(PlaceRequest.RequestType.ERROR, "Username already taken!"));
                }else{
                    sendMessage(new PlaceRequest(PlaceRequest.RequestType.LOGIN_SUCCESS, username));
                    PlaceServer.instance.addClient(username, this);
                    System.out.println("User: " + username + " connected");

                }
            }else if(request.getType() == PlaceRequest.RequestType.CHANGE_TILE){
                if(System.currentTimeMillis() - lastChangeTime >= 500) {
                    PlaceTile tile = (PlaceTile) request.getData();
                    PlaceServer.instance.updateTile(tile);
                    lastChangeTime = System.currentTimeMillis();
                }
                PlaceTile tile = (PlaceTile) request.getData();
                PlaceServer.instance.updateTile(tile);
            }
        }

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
                    handleMessage(request);
                    Thread.sleep(10);
                }
            }
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
