package minimetro;

import java.util.ArrayList;

/**
 * This class represents the terrain that contains the tracks, trains,
 * landscape, ...
 *
 * @author arthu
 */
public class World {

    private int nbRows, nbCols;
    private ArrayList<ArrayList<Cell>> cells;

    public World() {
        nbRows = 15;
        nbCols = 24;
        cells = new ArrayList<>();
        for (int row = 0; row < nbRows; row++) {
            cells.add(new ArrayList<>());
            for (int col = 0; col < nbCols; col++) {
                cells.get(row).add(new Cell());
            }
        }
    }

    public int getNbRows() {
        return nbRows;
    }

    public int getNbCols() {
        return nbCols;
    }

    public Cell getCell(int row, int col) {
        try {
            return cells.get(row).get(col);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void togglePlayPause() {
        System.out.println("World.togglePlayPause()");
    }

    void setCell(int row, int col, StationCell newCell) {
        try {
            cells.get(row).set(col, newCell);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    void setTrack(int row, int col, int prevRow, int prevCol) {
        Cell origin = getCell(prevRow, prevCol);
        Cell dest = getCell(row, col);
        origin.setTrack(row - prevRow, col - prevCol);
        dest.setTrack(prevRow - row, prevCol - col);
    }
}
