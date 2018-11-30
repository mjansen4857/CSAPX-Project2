package place.client.ptui;

import place.*;
import place.client.model.ClientModel;
import place.client.network.NetworkClient;
import place.network.PlaceRequest;

import java.io.IOException;
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
    private boolean firstUpdate = true;

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
                this.serverConn.game.addObserver( this );
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

        while(this.serverConn.game.isRunning()){
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

        System.out.println(serverConn.game.toString()+"\n");

        if(firstUpdate){
            firstUpdate = false;
            Thread userIn = new Thread( () -> this.run() );
            userIn.start();
        }
    }

    private void run() {
        Scanner in = new Scanner(System.in);
        int row;
        int col;
        PlaceColor color;
        while (this.serverConn.game.isRunning()) {

            System.out.println("Send move as: row col color");
            row = in.nextInt();
            if(row != -1) {
                col = in.nextInt();
                int colorNum = in.nextInt();
                color = PlaceColor.BLACK;
                //Get the right color, Black by default
                for (PlaceColor c : PlaceColor.values()) {
                    if (c.getNumber() == colorNum) {
                        color = c;
                        break;
                    }
                }
            }
            else{
                col = 0;
                color = PlaceColor.BLACK;
            }
            serverConn.sendMove(row, col, color);

            try{
                //Sleeps half a second after tiles are changed by the client
                Thread.sleep(500);
            }
            catch (InterruptedException e){}
        }
        System.out.println("Disconnected");
        System.exit(0);
    }

    public static void main(String[] args) {
        if(args.length != 3){
            System.err.println("Usage: java PlaceClient host port username");
            System.exit(0);
        }
        ConsoleApplication.launch(PlacePTUI.class, args);
    }
}
