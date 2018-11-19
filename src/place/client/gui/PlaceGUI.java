package place.client.gui;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Observable;
import java.util.Observer;

public class PlaceGUI extends Application implements Observer {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.show();
    }

    @Override
    public void update(Observable o, Object arg) {

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
