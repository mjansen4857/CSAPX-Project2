package place.client.bots;

import place.PlaceColor;
import place.PlaceException;
import place.client.model.ClientModel;
import place.client.network.NetworkClient;
import place.client.ptui.ConsoleApplication;
import place.client.ptui.PlacePTUI;

import java.io.PrintWriter;
import java.util.*;

public class RandomBot extends ConsoleApplication implements Observer {
    private ClientModel model;
    private NetworkClient serverConn;
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

        while(this.serverConn.game.isRunning()){
            try {
                this.wait();
            }
            catch( InterruptedException ie ) {}
        }
    }

    @Override
    public void stop() {
        this.serverConn.close();
    }

    @Override
    public void update(Observable t, Object o ) {

        assert t == this.model: "Update from non-model Observable";

        //System.out.println(serverConn.game.toString()+"\n");

        if(firstUpdate){
            firstUpdate = false;
            Thread userIn = new Thread( () -> this.run() );
            userIn.start();
        }
    }

    private void run() {
        Random random = new Random();

        while (this.serverConn.game.isRunning()) {
            PlaceColor color = PlaceColor.BLACK;
            int c = random.nextInt(PlaceColor.values().length);
            for(PlaceColor col : PlaceColor.values()){
                if(c == col.getNumber()){
                    color = col;
                    //break;
                }
            }
            serverConn.sendMove(random.nextInt(serverConn.game.getDim()), random.nextInt(serverConn.game.getDim()), color);
        }
        System.out.println("Disconnected");
        System.exit(0);
    }

    public static void main(String[] args) {
        if(args.length != 3){
            System.err.println("Usage: java RandomBot host port username");
            System.exit(0);
        }
        ConsoleApplication.launch(RandomBot.class, args);
    }
}
