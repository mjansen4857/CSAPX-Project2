package place.test;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class TestClient {
    public static void main(String[] args) {
        if(args.length != 2){
            System.err.println("Usage: java TestClient address port");
        }
        int port = Integer.parseInt(args[1]);
        try(Socket socket = new Socket(args[0], port);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Random rand = new Random();
            String username = "user" + rand.nextInt(10);
            out.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, username));
            PlaceBoard board = null;
            while (true) {
                PlaceRequest<?> request = (PlaceRequest<?>) in.readUnshared();
                if (request.getType() == PlaceRequest.RequestType.LOGIN_SUCCESS) {
                    System.out.println("Login Success");
                } else if (request.getType() == PlaceRequest.RequestType.ERROR){
                    System.err.println((String) request.getData());
                }else if(request.getType() == PlaceRequest.RequestType.BOARD){
                    board = (PlaceBoard) request.getData();
                    System.out.println("Board received: " + request.getData());
                }else if(request.getType() == PlaceRequest.RequestType.TILE_CHANGED){
                    board.setTile((PlaceTile) request.getData());
                    System.out.println(board);
                    System.out.println("Tile Changed: " + request.getData());
                }
                Thread.sleep(1000);
                out.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.CHANGE_TILE, new PlaceTile(rand.nextInt(10), rand.nextInt(10), username, PlaceColor.BLACK)));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
