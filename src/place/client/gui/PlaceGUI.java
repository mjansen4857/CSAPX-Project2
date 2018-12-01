package place.client.gui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.client.model.ClientModel;
import place.client.network.NetworkClient;

import java.io.BufferedWriter;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class PlaceGUI extends Application implements Observer {

    private ClientModel model;
    private NetworkClient serverConn;
    private BorderPane mainPane;
    private PlaceColor color = PlaceColor.BLACK;
    private boolean firstUpdate = true;

    public void init(){
        List< String > args = super.getParameters().getRaw();

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

        this.model.addObserver( this );
    }

    @Override
    public synchronized void start(Stage mainStage) throws Exception {
        Thread.sleep(500);
        mainPane = new BorderPane();

        GridPane grid = new GridPane();
        for (int i = 0; i < model.getDim(); i++) {
            for (int j = 0; j < model.getDim(); j++) {
                Button btn = new Button("");
                btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                int row = i;
                int column = j;
                btn.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent e) {
                                        serverConn.sendMove(row, column, color);
                                        try {Thread.sleep(500); }
                                        catch (InterruptedException er){}
                                    }
                });

                String hexColor = hexColor(model.getTile(j,i).getColor());
                btn.setStyle("-fx-background-color: #" + hexColor + "; ");

                grid.add(btn,j,i);
            }
        }

        GridPane bottom = new GridPane();
        for(int i=0; i<16; i++){

            Button btn = new Button(Integer.toString(i+1));

            int x = i;
            btn.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) { color = getColor(x); }
            });

            String hexColor = hexColor(getColor(i));
            btn.setStyle("-fx-background-color: #" + hexColor + "; ");
            bottom.add(btn, i, 0);
        }

        mainPane.setCenter(grid);
        mainPane.setBottom(bottom);
        Scene scene = new Scene(mainPane, 600, 600);
        mainStage.setScene(scene);
        mainStage.show();
    }

    @Override
    public void update(Observable t, Object o) {

        assert t == this.model: "Update from non-model Observable";
        if(firstUpdate){firstUpdate = false;}
        else{
            for (int i = 0; i < model.getDim(); i++) {
                for (int j = 0; j < model.getDim(); j++) {
                    String hexColor = hexColor(model.getTile(j, i).getColor());
                    getButtonFromGridPane((GridPane) mainPane.getCenter(), i, j).setStyle("-fx-background-color: #" + hexColor + "; ");
                }
            }
        }

    }

    private Button getButtonFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return (Button) node;
            }
        }
        return null;
    }

    private String hexColor(PlaceColor col){
        String red = Integer.toHexString(col.getRed());
        if (red.length() == 1) {
            red = "0" + red;
        }
        String green = Integer.toHexString(col.getGreen());
        if (green.length() == 1) {
            green = "0" + green;
        }
        String blue = Integer.toHexString(col.getBlue());
        if (blue.length() == 1) {
            blue = "0" + blue;
        }
        return red + green + blue;
    }

    private PlaceColor getColor(int i){
        for (PlaceColor c : PlaceColor.values()) { if (c.getNumber() == i) { return c; } }
        return null;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceGUI host port username");
            System.exit(-1);
        } else {
            Application.launch(args);
        }
    }
}
