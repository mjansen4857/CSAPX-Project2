package place.client.ptui;

import place.*;
import place.network.PlaceRequest;
import place.server.PlaceServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

public class PlacePTUI extends ConsoleApplication implements Observer {

    private Board model;
    private NetworkClient serverConn;
    private Scanner userIn;
    private PrintWriter userOut;

    public void init() {
            List< String > args = super.getArguments();

            // Get host info from command line
            String host = args.get( 0 );
            int port = Integer.parseInt( args.get( 1 ) );
            this.model = new Board();

            // Create the network connection.
            try{
                this.serverConn = new NetworkClient(host, port, this.model);
            }
            catch(PlaceException e){
                System.out.println(e);
                System.exit(-1);
            }
    }

    @Override
    public synchronized void go(Scanner userIn, PrintWriter userOut) {
        this.userIn = userIn;
        this.userOut = userOut;
    }

    @Override
    public void stop() {
        this.userIn.close();
        this.userOut.close();
        //this.serverConn.close();
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
