package place.server;

import place.PlaceTile;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ServerStatistics {

    private PlaceServer server;
    private ArrayList<PlaceTile>[][] tiles;
    private HashMap<String, Integer> userChanges;

    public ServerStatistics(PlaceServer server){
        this.server = server;
        this.tiles = new ArrayList[server.board.DIM][server.board.DIM];
        this.userChanges = new HashMap<>();

        for(int i=0; i<tiles.length; i++){
            for(int j=0; j<tiles[i].length; j++){
                tiles[i][j] = new ArrayList<>();
                tiles[i][j].add(tiles[i][j].size(), server.board.getTile(i,j));
            }
        }
    }

    public void changeTile(PlaceTile tile){

        tiles[tile.getRow()][tile.getCol()].add(tile);
        if(userChanges.containsKey(tile.getOwner())){ userChanges.put(tile.getOwner(), userChanges.get(tile.getOwner()) + 1); }
        else{ userChanges.put(tile.getOwner(), 1); }

    }

    public void generateReport() throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("ServerStatistics.txt"), "utf-8"));

        writer.write("Statistics for PlaceServer:");
        writer.newLine();
        writer.newLine();
        writer.write("Server start time: " + new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(new Date(server.startTime)));
        writer.newLine();
        writer.write("Server end time: " + new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(new Date(server.endTime)));
        writer.newLine();
        int seconds = (int) (((server.endTime-server.startTime) / 1000) % 60) ;
        int minutes = (int) (((server.endTime-server.startTime) / (1000*60)) % 60);
        int hours   = (int) (((server.endTime-server.startTime) / (1000*60*60)) % 24);
        String hr = " hours ";
        String min = " minutes ";
        String sec = " seconds ";
        if(hours == 1) hr = " hour ";
        if(minutes == 1) min = " minute ";
        if(seconds == 1) sec = " second ";
        writer.write("Server run time: " + hours + hr + minutes + min + seconds + sec);





        writer.close();
    }
}
