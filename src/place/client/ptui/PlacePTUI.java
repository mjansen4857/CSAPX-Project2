package place.client.ptui;

import java.util.Observable;
import java.util.Observer;

public class PlacePTUI implements Observer {
    @Override
    public void update(Observable o, Object arg) {

    }
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceClient host port username");
        }
    }
}
