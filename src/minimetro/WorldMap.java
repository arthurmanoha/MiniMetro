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
        walkwayList = new ArrayList<>();
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
        computeWalkways();
    }

    public String toFormattedString() {
        String res = "<html>";

        for (Walkway w : walkwayList) {
            res += "Walkway: " + w.getStation(0) + " - " + w.getStation(1) + "<br/>";
        }

        for (int key : linesList.keySet()) {
            TrainLine currentLine = linesList.get(key);
            res += "Loco " + key + ": {" + currentLine.getAllStationsString() + "}<br/>";
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
                        currentCell.addWalkwayDirection((StationCell) cellEast, CardinalPoint.EAST);
                        cellEast.addWalkwayDirection((StationCell) currentCell, CardinalPoint.WEST);
                    }
                    Cell cellSouth = w.getCell(row + 1, col);
                    if (cellSouth != null && cellSouth instanceof StationCell) {
                        walkwayList.add(new Walkway(((StationCell) currentCell).getId(),
                                ((StationCell) cellSouth).getId()));
                        currentCell.addWalkwayDirection((StationCell) cellSouth, CardinalPoint.SOUTH);
                        cellSouth.addWalkwayDirection((StationCell) currentCell, CardinalPoint.NORTH);
                    }
                }
            }
        }
    }

    /**
     * Tell the passenger what trains and walkways to use in order to reach
     * their target station
     *
     * @param currentStationId
     * @param p
     *
     * Example of map:
     * Walkway: 1 - 2
     * Walkway: 8 - 9
     * Walkway: 5 - 4
     * Loco 0: {0 - 1 - 8 - 7}
     * Loco 20: {5 - 6}
     * Loco 5: {1 - 8 - 7 - 0}
     * Loco 10: {3 - 4 - 2}
     * Loco 15: {10 - 9}
     */
    protected void computePath(int currentStationId, Passenger p) {

        ArrayList<PathTree> openList = new ArrayList<>();
        ArrayList<PathTree> closedList = new ArrayList<>();

        openList.add(new PathTree(currentStationId, null));

        PathTree currentNode;
        boolean success = false;
        // LOOP
        do {
            currentNode = openList.remove(0);
            closedList.add(currentNode);
            for (int neighborId : getNeighbors(currentNode.getStationId())) {
                // Add only stations not already in closed or open list.
                if (!isInList(neighborId, openList) && !isInList(neighborId, closedList)) {
                    openList.add(new PathTree(neighborId, currentNode));
                }
            }
            success = (currentNode.getStationId() == p.getTargetStationId());
        } while (!success && !openList.isEmpty());
        // END LOOP
        if (success) {
            p.clearPath();
            while (currentNode != null) {
                int stationId = currentNode.getStationId();
                if (stationId != currentStationId) {
                    p.addPathStep(stationId);
                }
                currentNode = currentNode.getPrev();
            }
        }
    }

    private boolean isInList(int id, ArrayList<PathTree> list) {
        for (PathTree tree : list) {
            if (tree.getStationId() == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all the stations reachable by train or walkway from the specified
     * station.
     *
     * @param currentStationId
     * @return
     */
    private ArrayList<Integer> getNeighbors(int currentStationId) {

        ArrayList<Integer> neighbors = new ArrayList<>();

        // Get all the other stations reachable via the same line (if we find a line that contains it).
        TrainLine currentLine = findCurrentLine(currentStationId);
        if (currentLine != null) {
            for (StationCell station : currentLine.getAllStations()) {
                int otherStationId = station.getId();
                if (otherStationId != currentStationId) {
                    neighbors.add(otherStationId);
                }
            }
        }
        // Get all the stations reachable via a walkway.
        for (Walkway w : walkwayList) {
            if (w.contains(currentStationId)) {
                int otherStationId = w.getOtherStation(currentStationId);
                neighbors.add(otherStationId);
            }
        }

        return neighbors;
    }

    private TrainLine findCurrentLine(int currentStationId) {
        TrainLine result = null;
        for (int lineNumber : linesList.keySet()) {
            TrainLine line = linesList.get(lineNumber);
            if (line.containsStation(currentStationId)) {
                result = line;
            }
        }
        return result;
    }

}
