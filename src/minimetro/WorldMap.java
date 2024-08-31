package minimetro;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is a logical representation of the train lines.
 *
 * @author arthu
 */
public class WorldMap {

    private HashMap<Integer, TrainLine> linesList; // For any Loco id, there is one TrainLine.
    private ArrayList<Walkway> walkwayList;
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

        for (Walkway w : walkwayList) {
            res += "Walkway: " + w.getStation(0) + " - " + w.getStation(1) + "<br/>";
        }

        for (int key : linesList.keySet()) {
            TrainLine currentLine = linesList.get(key);
            res += "Line " + key + ": {" + currentLine.getAllStations() + "}<br/>";
        }
        res += "</html>";
        return res;
    }

    protected void computeWalkways() {
        walkwayList = new ArrayList<>();
        for (int row = 0; row < w.getNbRows(); row++) {
            for (int col = 0; col < w.getNbCols(); col++) {
                // If station (row, col) is located next to another station to its east or south, then we create a walkway.
                Cell currentCell = w.getCell(row, col);
                if (currentCell instanceof StationCell) {
                    Cell cellEast = w.getCell(row, col + 1);
                    if (cellEast != null && cellEast instanceof StationCell) {
                        walkwayList.add(new Walkway(((StationCell) currentCell).getId(),
                                ((StationCell) cellEast).getId()));
                    }
                    Cell cellSouth = w.getCell(row + 1, col);
                    if (cellSouth != null && cellSouth instanceof StationCell) {
                        walkwayList.add(new Walkway(((StationCell) currentCell).getId(),
                                ((StationCell) cellSouth).getId()));
                    }
                }
            }
        }
    }
}
