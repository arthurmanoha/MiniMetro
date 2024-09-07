package minimetro;

import java.util.ArrayList;

/**
 * This class logically represents the path of a single train.
 *
 * @author arthu
 */
public class TrainLine {

    // Coordinates of the ids of the stations served by this line.
    private ArrayList<StationCell> cells;

    // The id of the loco which discovered this line.
    private int locoId;

    public TrainLine(int newLocoId) {
        cells = new ArrayList<>();
        locoId = newLocoId;
    }

    /**
     * Add a Cell to a line.
     *
     * @param c the cell added. If it already exists, we move it to the last
     * position.
     */
    protected void addCell(StationCell c) {
        if (c != null) {
            if (cells.contains(c)) {
                cells.remove(c);
            }
            int index = cells.size();
            cells.add(index, c);
        }
    }

    protected boolean containsCell(StationCell candidate) {
        for (Cell c : cells) {
            if (candidate.equals(c)) {
                return true;
            }
        }
        return false;
    }

    protected boolean containsStation(int stationId) {
        boolean res = false;
        for (StationCell c : cells) {
            if (c.getId() == stationId) {
                res = true;
            }
        }
        return res;
    }

    public int getLocoId() {
        return locoId;
    }

    protected String getAllStationsString() {

        String result = "";
        for (StationCell c : cells) {
            int id = c.getId();
            if (result.isEmpty()) {
                result += id + "";
            } else {
                result += " - " + id;

            }
        }
        return result;
    }

    protected ArrayList<Integer> getAllStationsIds() {
        ArrayList<Integer> result = new ArrayList<>();
        for (StationCell c : cells) {
            result.add(c.getId());
        }
        return result;
    }

    protected ArrayList<StationCell> getAllStations() {
        return cells;
    }

    @Override
    public String toString() {
        String text = "L_" + locoId + "";
        for (Cell c : cells) {
            if (c instanceof StationCell) {
                text += " " + ((StationCell) c).getId();
            }
        }
        return text;
    }
}
