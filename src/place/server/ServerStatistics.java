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

/**
 * TODO
 * @author Tyson Levy
 */
public class ServerStatistics {

    private PlaceServer server;
    private ArrayList<PlaceTile>[][] tiles;
    private HashMap<String, Integer> userChanges;

    /**
     * TODO
     * @param server
     */
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

    /**
     * TODO
     * @param tile
     */
    public void changeTile(PlaceTile tile){

        tiles[tile.getRow()][tile.getCol()].add(tile);
        if(userChanges.containsKey(tile.getOwner())){ userChanges.put(tile.getOwner(), userChanges.get(tile.getOwner()) + 1); }
        else{ userChanges.put(tile.getOwner(), 1); }

    }

    /**
     * TODO
     * @throws IOException
     */
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
        String mins = " minutes ";
        String sec = " seconds ";
        if(hours == 1) hr = " hour ";
        if(minutes == 1) mins = " minute ";
        if(seconds == 1) sec = " second ";
        writer.write("Server run time: " + hours + hr + minutes + mins + seconds + sec);
        writer.newLine();
        writer.newLine();

        int max = 0;
        int min = 0;
        int total = 0;
        String mostChanges = "";
        String leastChanges = "";
        if(userChanges.size() != 0){
            max = userChanges.get(userChanges.keySet().toArray()[0]);
            min = userChanges.get(userChanges.keySet().toArray()[0]);
            for(String user : userChanges.keySet()){
                total += userChanges.get(user);
                if (userChanges.get(user) > max) { max = userChanges.get(user); }
                if (userChanges.get(user) < min) { min = userChanges.get(user); }
            }
            for(String user : userChanges.keySet()){
                if(userChanges.get(user) == max){ mostChanges += user + " "; }
                if(userChanges.get(user) == min){ leastChanges += user + " "; }
            }
        }
        writer.write("Users with the most changes (" + max + " changes): " + mostChanges);
        writer.newLine();
        writer.write("Users with the least changes (" + min + " changes): " + leastChanges);
        writer.newLine();
        writer.write("Average changes per minute: " + total / (((double)(server.endTime-server.startTime)) /((double) (1000*60))));
        writer.newLine();
        writer.close();
    }
}
