package place.client.bots;

import place.PlaceColor;
import place.PlaceException;
import place.client.model.ClientModel;
import place.client.network.NetworkClient;
import place.client.ptui.ConsoleApplication;
import java.io.PrintWriter;
import java.util.*;

/**
 * A bot that randomly makes tile changes and send them to a PlaceServer
 *
 * @author Tyson Levy
 */
public class RandomBot extends ConsoleApplication implements Observer {
    private ClientModel model;
    private NetworkClient serverConn;
    private boolean firstUpdate = true;

    /**
     * Initializes the client by starting up the connection with a Network Client
     * also initializes the model
     */
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

    /**
     * Starts up the BOT and keeps it running until the connection closes
     *
     * @param userIn
     * @param userOut
     */
    @Override
    public synchronized void go(Scanner userIn, PrintWriter userOut) {

        while(this.serverConn.game.isRunning()){
            try {
                this.wait();
            }
            catch( InterruptedException ie ) {}
        }
    }

    /**
     * Starts to close the client
     */
    @Override
    public void stop() {
        this.serverConn.close();
    }

    /**
     * Does nothing when called because the bot has no need to "see" the board
     * @param t
     * @param o
     */
    @Override
    public void update(Observable t, Object o ) {

        assert t == this.model: "Update from non-model Observable";

        if(firstUpdate){
            firstUpdate = false;
            Thread userIn = new Thread( () -> this.run() );
            userIn.start();
        }
    }

    /**
     * Creates random tiles and send them to the NetworkClient
     */
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
