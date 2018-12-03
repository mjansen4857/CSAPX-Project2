package place.client.gui;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
    private Node lastToggle;
    private Canvas canvas;
    private static final double SIZE = 600;

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
    public synchronized void start(Stage mainStage) {
        while(!serverConn.isLoaded()) {}
        mainPane = new BorderPane();

//        ToggleGroup tg = new ToggleGroup();
//        tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
//            public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) {
//                try {
//                    serverConn.sendMove(GridPane.getRowIndex((Node) tg.getSelectedToggle()), GridPane.getColumnIndex((Node) tg.getSelectedToggle()), color);
//                    lastToggle = (Node) tg.getSelectedToggle();
//                }
//                catch (NullPointerException e){serverConn.sendMove(GridPane.getRowIndex(lastToggle), GridPane.getColumnIndex(lastToggle), color);}
//            }
//        });
//
//        GridPane grid = new GridPane();
//        for (int i = 0; i < model.getDim(); i++) {
//            for (int j = 0; j < model.getDim(); j++) {
//                Rectangle rec = new Rectangle(SIZE / model.getDim(), SIZE / model.getDim());
//                ToggleButton btn = new ToggleButton("");
//                btn.setPrefSize(SIZE / model.getDim(), SIZE / model.getDim());
//
//                String hexColor = hexColor(model.getTile(j, i).getColor());
//                rec.setFill(Color.valueOf(hexColor));
//                btn.setToggleGroup(tg);
//
//                btn.setOpacity(0);
//                //btn.setContentAreaFilled(false);
//                //btn.setBorderPainted(false);
//
//                grid.add(rec, j, i);
//                grid.add(btn,j,i);
//
//            }
//        }

        this.canvas = new Canvas(SIZE, SIZE);
        canvas.setOnMouseClicked((event) -> {
            if(event.getButton() == MouseButton.PRIMARY){
                double size = SIZE/model.getDim();
                int row = (int) (event.getY()/size);
                int col = (int) (event.getX()/size);
                serverConn.sendMove(row, col, color);
            }
        });
        drawBoard();

        HBox bottom = new HBox();
        for (int i = 0; i < 16; i++) {

            Button btn = new Button(Integer.toString(i));

            int x = i;
            btn.setOnAction((event) -> color = getColor(x));

            String hexColor = hexColor(getColor(i));
            btn.setStyle("-fx-background-color: #" + hexColor + "; ");
            if(i == 0 || i == 4 || (i >= 12 && i <= 14)){
                btn.setStyle(btn.getStyle() + "-fx-text-fill: #ffffff");
            }
            btn.setPrefWidth(SIZE / 16);
            bottom.getChildren().add(btn);
        }

        mainPane.setCenter(canvas);
        mainPane.setBottom(bottom);
        Scene scene = new Scene(mainPane, SIZE, SIZE + 25);

        mainStage.setScene(scene);
        mainStage.show();
    }

    @Override
    public void stop(){
        serverConn.close();
    }

    @Override
    public void update(Observable t, Object o) {
        assert t == this.model: "Update from non-model Observable";
        if(firstUpdate){firstUpdate = false;}
        else{
            PlaceTile tile = model.getLastTileChanged();
            updateCanvas(tile);
//            String hexColor = hexColor(tile.getColor());
//            getRectFromGridPane((GridPane) mainPane.getCenter(), tile.getCol(), tile.getRow()).setFill(Color.valueOf(hexColor));
        }
    }

    private void drawBoard(){
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, SIZE, SIZE);
        double size = SIZE/model.getDim();
        for(int i = 0; i < model.getDim(); i++){
            for(int j = 0; j < model.getDim(); j++){
                PlaceTile tile = model.getTile(i, j);
                g.setFill(Color.valueOf(hexColor(tile.getColor())));
                g.fillRect(size*j, size*i, size, size);
            }
        }
    }

    private void updateCanvas(PlaceTile tile){
        GraphicsContext g = canvas.getGraphicsContext2D();
        double size = SIZE/model.getDim();
        g.setFill(Color.valueOf(hexColor(tile.getColor())));
        g.fillRect(tile.getCol()*size, tile.getRow()*size, size, size);
    }

    private Rectangle getRectFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                if(node instanceof Rectangle){return (Rectangle) node;}
            }
        }
        return null;
    }

    private ToggleButton getButtonFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                if(node instanceof ToggleButton){return (ToggleButton) node;}
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
