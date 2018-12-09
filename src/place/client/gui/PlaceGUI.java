package place.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.client.model.ClientModel;
import place.client.network.NetworkClient;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * A GUI client that interfaces with a running place server
 *
 * @author Michael Jansen
 * @author Tyson Levy
 * @author Leelan Carbonell
 */
public class PlaceGUI extends Application implements Observer {
    private ClientModel model;
    private NetworkClient serverConn;
    private BorderPane mainPane;
    private PlaceColor color = PlaceColor.BLACK;
    private boolean firstUpdate = true;
    private Canvas canvas;
    private static final double SIZE = 600;
    private Tooltip tp;
    private double scale = 1;
    private Point anchor = new Point(0, 0);
    private Point dragStart = new Point();

    /**
     * Initializes the client by starting up the connection with a Network Client
     * also initializes the model
     */
    @Override
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

    /**
     * Update the tile information tooltip
     * @param text The text of the tooltip
     * @param x X position
     * @param y Y position
     */
    private void updateTooltip(String text, double x, double y){
        tp.setText(text);
        tp.show(mainPane, x, y);
    }

    /**
     * Creates the GUI and sets everything up
     *
     * @param mainStage
     */
    @Override
    public synchronized void start(Stage mainStage) {
        while(!serverConn.isLoaded()) {}
        mainPane = new BorderPane();
        mainStage.setTitle("Place: " + serverConn.getUsername());
        tp = new Tooltip();

        this.canvas = new Canvas(SIZE, SIZE);
        // Change the tile that the user clicks on
        canvas.setOnMouseClicked((event) -> {
            if(event.getButton() == MouseButton.PRIMARY){
                double size = SIZE/model.getDim();
                int row = (int) ((event.getY()+anchor.getY())/(size*scale));
                int col = (int) ((event.getX()+anchor.getX())/(size*scale));
                serverConn.sendMove(row, col, color);
            }
        });
        // Scale the board when the user scrolls over it
        canvas.setOnScroll((event) -> {
            if(event.getDeltaY() < 0){
                scale -= 0.001 * model.getDim()/20 * Math.abs(event.getDeltaY());
            }else{
                scale += 0.001 * model.getDim()/20 * Math.abs(event.getDeltaY());
            }
            scale = Math.max(1, scale);
            drawBoard();
        });
        // Set the start position of a drag
        canvas.setOnMousePressed((event) -> {
            if(event.getButton() == MouseButton.SECONDARY){
                dragStart.x = (int) event.getX();
                dragStart.y = (int) event.getY();
                System.out.println(dragStart);
            }
        });
        // Drag the board around on right click
        canvas.setOnMouseDragged((event) -> {
            if(event.getButton() == MouseButton.SECONDARY){
                anchor.x -= (event.getX() - dragStart.getX());
                anchor.y -= (event.getY() - dragStart.getY());
                anchor.x = Math.max(0, anchor.x);
                anchor.y = Math.max(0, anchor.y);
                dragStart.x = (int) event.getX();
                dragStart.y = (int) event.getY();
                drawBoard();
            }
        });

        // This thread gets the location of the mouse and shows a tooltip that
        // gives the info of the tile it is hovering over
        new Thread(() -> {
            double size = SIZE / model.getDim();
            while (serverConn.goodToGo()) {
                Point p = MouseInfo.getPointerInfo().getLocation();
                if (p.getX() >= mainStage.getX() + 10 && p.getX() <= mainStage.getX() + mainStage.getWidth() - 10) {
                    if (p.getY() >= mainStage.getY() + 30 && p.getY() <= mainStage.getY() + mainStage.getHeight() - 40) {
                        int row = (int) (((p.getY() - mainStage.getY() - 28)+anchor.getY()) / (size*scale));
                        int col = (int) (((p.getX() - mainStage.getX() - 6)+anchor.getX()) / (size*scale));
                        if(row < model.getDim() && col < model.getDim() && row >= 0 && col >= 0) {
                            PlaceTile tile = model.getTile(row, col);
                            Platform.runLater(() -> updateTooltip("Pos: (" + row + "," + col + ")\n" +
                                    "Owner: " + tile.getOwner() + "\n" +
                                    "Time: " + new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(new Date(tile.getTime())) + "\n" +
                                    "Color: " + tile.getColor().getName(), p.getX() + 10, p.getY() + 10));
                        }else {
                            Platform.runLater(() -> tp.hide());
                        }
                    } else {
                        Platform.runLater(() -> tp.hide());
                    }
                } else {
                    Platform.runLater(() -> tp.hide());
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        drawBoard();

        //Buttons on the bottom for choosing the color
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

    /**
     * Starts to shut down the Client
     */
    @Override
    public void stop(){
        serverConn.close();
    }

    /**
     * Updates the GUI
     * Called when any change is sent to the server
     *
     * @param t
     * @param o
     */
    @Override
    public void update(Observable t, Object o) {
        assert t == this.model: "Update from non-model Observable";
        if(firstUpdate){firstUpdate = false;}
        else{
            PlaceTile tile = model.getLastTileChanged();
            updateCanvas(tile);
        }
    }

    /**
     * Draw the place board on the canvas
     */
    private void drawBoard(){
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, SIZE, SIZE);
        double size = SIZE/model.getDim();
        for(int i = 0; i < model.getDim(); i++){
            for(int j = 0; j < model.getDim(); j++){
                PlaceTile tile = model.getTile(i, j);
                g.setFill(Color.valueOf(hexColor(tile.getColor())));
                g.fillRect(size*j*scale - anchor.getX(), size*i*scale - anchor.getY(), size*scale, size*scale);
            }
        }
    }

    /**
     * Redraw the area of the canvas where a tile was updated
     * @param tile the updated tile
     */
    private void updateCanvas(PlaceTile tile){
        GraphicsContext g = canvas.getGraphicsContext2D();
        double size = SIZE/model.getDim();
        g.setFill(Color.valueOf(hexColor(tile.getColor())));
        g.fillRect(tile.getCol()*size*scale - anchor.getX(), tile.getRow()*size*scale - anchor.getY(), size*scale, size*scale);
    }

    /**
     * Convert a place tile color to a hex string
     * @param col The color of the tile
     * @return The hex color string
     */
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

    /**
     * Get a place color from the integer it represents
     * @param i The int
     * @return The place color
     */
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
