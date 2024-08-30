package minimetro;

import java.util.ArrayList;

/**
 * This class logically represents a train line with its stations and its
 * connections.
 *
 * @author arthu
 */
public class TrainLine {

    // Coordinates of the ids of the stations served by this line.
    private ArrayList<Cell> cells;

    // The id of the loco which discovered this line.
    private int locoId;

    public TrainLine(int newLocoId) {
        cells = new ArrayList<>();
        locoId = newLocoId;
    }

    protected void addCell(Cell c) {
        if (!cells.contains(c)) {
            cells.add(c);
        }
    }

    protected boolean containsCell(Cell candidate) {
        for (Cell c : cells) {
            if (candidate.equals(c)) {
                return true;
            }
        }
        return false;
    }

    public int getLocoId() {
        return locoId;
    }

    protected String getAllStations() {

        String result = "";
        for (Cell c : cells) {
            if (c instanceof StationCell) {
                int id = ((StationCell) c).getId();
                if (result.isEmpty()) {
                    result += id + "";
                } else {
                    result += " - " + id;
                }
            }
        }
        return result;
    }
}
