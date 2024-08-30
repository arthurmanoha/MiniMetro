package minimetro;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is a logical representation of the train lines.
 *
 * @author arthu
 */
public class WorldMap {

//    private ArrayList<TrainLine> linesList;
    private HashMap<Integer, TrainLine> linesList; // For any Loco id, there is one TrainLine.
    private World w;

    public WorldMap(World newW) {
        w = newW;
        linesList = new HashMap<>();
    }

    protected void addStation(StationCell station, int trainElementId) {

        TrainLine line;
        if (linesList.containsKey(trainElementId)) {
            line = linesList.get(trainElementId);
        } else {
            // Add a new line for that loco
            line = new TrainLine(trainElementId);
            linesList.put(trainElementId, line);
        }
        line.addCell(station);
    }

    public String toFormattedString() {
        String res = "<html>";
        for (int key : linesList.keySet()) {
            res += "Line " + key + ": {" + linesList.get(key).getAllStations() + "}<br/>";
        }
        res += "</html>";
        return res;
    }
}
