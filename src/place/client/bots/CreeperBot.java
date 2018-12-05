package place.client.bots;

import place.PlaceColor;
import place.PlaceException;
import place.client.model.ClientModel;
import place.client.network.NetworkClient;
import place.client.ptui.ConsoleApplication;

import java.io.PrintWriter;
import java.util.*;

public class CreeperBot extends ConsoleApplication implements Observer {
    private ClientModel model;
    private NetworkClient serverConn;
    private boolean firstUpdate = true;

    private PlaceColor[][] picture;

    public void init() {

        picture = new PlaceColor[8][8];

        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                if((j + i) % 2 == 0) picture[i][j] = PlaceColor.GREEN;
                else           picture[i][j] = PlaceColor.LIME;
            }
        }
        picture[2][1] = PlaceColor.BLACK;
        picture[2][2] = PlaceColor.BLACK;
        picture[2][5] = PlaceColor.BLACK;
        picture[2][6] = PlaceColor.BLACK;
        picture[3][1] = PlaceColor.BLACK;
        picture[3][2] = PlaceColor.BLACK;
        picture[3][5] = PlaceColor.BLACK;
        picture[3][6] = PlaceColor.BLACK;
        picture[4][3] = PlaceColor.BLACK;
        picture[4][4] = PlaceColor.BLACK;
        picture[5][2] = PlaceColor.BLACK;
        picture[5][3] = PlaceColor.BLACK;
        picture[5][4] = PlaceColor.BLACK;
        picture[5][5] = PlaceColor.BLACK;
        picture[6][2] = PlaceColor.BLACK;
        picture[6][3] = PlaceColor.BLACK;
        picture[6][4] = PlaceColor.BLACK;
        picture[6][5] = PlaceColor.BLACK;
        picture[7][2] = PlaceColor.BLACK;
        picture[7][5] = PlaceColor.BLACK;

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
        Scanner in = new Scanner(System.in);
        System.out.println("Enter location for picture as: row col");
        int row = in.nextInt();
        int col = in.nextInt();
        if(row + picture.length >= serverConn.game.getDim() || col + picture[0].length >= serverConn.game.getDim()){ row = 0; col = 0; }
        if( row >= serverConn.game.getDim() || col >= serverConn.game.getDim()) {
            System.out.println("Board to small for picture");
            System.exit(0);
        }
        while (serverConn.game.isRunning()){
            for (int i=row; i<row+picture.length; i++){
                for(int j=col; j<col+picture[0].length; j++) {
                    if (model.getTile(i, j).getColor() != picture[i - row][j - col]) {
                        serverConn.sendMove(i, j, picture[i - row][j - col]);
                        try { Thread.sleep(500); } catch (InterruptedException e) { }
                    }
                }
            }
        }

        System.out.println("Disconnected");
        System.exit(0);
    }

    public static void main(String[] args) {
        if(args.length != 3){
            System.err.println("Usage: java CreeperBot host port username");
            System.exit(0);
        }
        ConsoleApplication.launch(CreeperBot.class, args);
    }
}
