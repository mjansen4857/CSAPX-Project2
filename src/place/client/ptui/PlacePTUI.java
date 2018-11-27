package place.client.ptui;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

public class PlacePTUI extends ConsoleApplication implements Observer {
    private PlaceBoard board;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;


    @Override
    public void go(Scanner consoleIn, PrintWriter consoleOut) {

    }

    @Override
    public void update( Observable t, Object o ) {

    }

    public static void main(String[] args) {
        if(args.length != 3){
            System.err.println("Usage: java PlaceClient host port username");
            System.exit(0);
        }
        ConsoleApplication.launch(PlacePTUI.class, args);
    }
}
