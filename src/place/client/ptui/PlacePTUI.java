package place.client.ptui;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Scanner;

public class PlacePTUI implements Observer {

    /**
     * Update the UI when the model calls notify.
     * Currently no information is passed as to what changed,
     * so everything is redone.
     *
     * @param t An Observable -- assumed to be the model.
     * @param o An Object -- not used.
     */
    @Override
    public void update( Observable t, Object o ) {
        System.out.println();
    }
    public static void main(String[] args) {
        if(args.length != 3){
            System.err.println("Usage: java PlaceClient host port username");
        }
        boolean loggedIn = false;
        boolean readyToSend = false;
        Scanner userIn = new Scanner(System.in);
        String userInput;
        int port = Integer.parseInt(args[1]);
        try(Socket socket = new Socket(args[0], port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            String username = args[2];
            out.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, username));
            while (true) {
                PlaceRequest<?> request= (PlaceRequest<?>) in.readUnshared();
                if (request.getType() == PlaceRequest.RequestType.LOGIN_SUCCESS) {
                    System.out.println("Login Success");
                    loggedIn = true;

                } else if (request.getType() == PlaceRequest.RequestType.ERROR) {
                    System.err.println((String) request.getData());
                } else if (request.getType() == PlaceRequest.RequestType.BOARD) {
                    System.out.println("Board received: " + request.getData());
                    System.out.println("Please enter your change as: row column color");
                    readyToSend = true;
                } else if (request.getType() == PlaceRequest.RequestType.TILE_CHANGED) {
                    System.out.println("Tile Changed: " + request.getData());
                    //Thread.sleep(500);
                }
                if(readyToSend){
                    int row = userIn.nextInt();
                    int column = userIn.nextInt();
                    int color = userIn.nextInt();
                    PlaceColor color1 = PlaceColor.BLACK;
                    for(PlaceColor c : PlaceColor.values()){
                        if(c.getNumber() == color){color1 = c;}
                    }
                    out.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.CHANGE_TILE, new PlaceTile(row, column, username, color1)));
                    readyToSend = false;
                }
                //Thread.sleep(10000);
                //out.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.CHANGE_TILE, new PlaceTile(rand.nextInt(10), rand.nextInt(10), username, PlaceColor.BLACK)));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
