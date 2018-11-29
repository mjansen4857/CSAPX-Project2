package place.client.ptui;

import place.*;
import place.client.model.ClientModel;
import place.client.network.NetworkClient;
import place.network.PlaceRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class PlacePTUI extends ConsoleApplication implements Observer {

    private ClientModel model;
    private NetworkClient serverConn;
    private Scanner userIn;
    private PrintWriter userOut;

    public void init() {

            List< String > args = super.getArguments();

            // Get host info from command line
            String host = args.get( 0 );
            int port = Integer.parseInt( args.get( 1 ) );
            String username = args.get(2);

            this.model = new ClientModel();

            // Create the network connection.
            try{
                this.serverConn = new NetworkClient(host, port, username, this.model);
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

        this.model.addObserver( this );

        while(true){
            try {
                this.wait();
            }
            catch( InterruptedException ie ) {}
        }
    }

    @Override
    public void stop() {
        this.userIn.close();
        this.userOut.close();
        this.serverConn.close();
    }

    @Override
    public void update( Observable t, Object o ) {

        assert t == this.model: "Update from non-model Observable";

        System.out.println("Board Updated");
        System.out.println(model.toString());
    }

    public static void main(String[] args) {
        if(args.length != 3){
            System.err.println("Usage: java PlaceClient host port username");
            System.exit(0);
        }
        ConsoleApplication.launch(PlacePTUI.class, args);
    }
}
