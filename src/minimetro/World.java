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

    protected void setCell(int row, int col, Cell newCell) {
        try {
            cells.get(row).set(col, newCell);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("World.setCell: error");
        }
    }

    protected void toggleStation(int row, int col) {
        Cell oldCell = this.getCell(row, col);
        if (oldCell instanceof StationCell) {
            this.setCell(row, col, new Cell());
        } else {
            this.setCell(row, col, new StationCell());
        }
    }

    /**
     * Add a locomotive if the designated cell has rails.
     *
     * @param row
     * @param col
     */
    protected void addLoco(int row, int col) {
        Cell c = getCell(row, col);
        c.setLoco();
    }

    /**
     * Add a carriage to the end of the train under construction.
     *
     * @param row
     * @param col
     */
    protected void addWagon(int row, int col) {
        Cell c = getCell(row, col);
        c.setWagon();
    }

    /**
     * The previous cell must create a track between the current and prevPrev
     * cells.
     */
    protected void setNewTrack(int colBefore, int rowBefore, int colCenter, int rowCenter, int colAfter, int rowAfter) {
        Cell newTrackCell = getCell(rowCenter, colCenter);

        int dxBefore = colBefore - colCenter;
        int dyBefore = rowBefore - rowCenter;
        int dxAfter = colAfter - colCenter;
        int dyAfter = rowAfter - rowCenter;

        try {
            newTrackCell.setTrack(dxBefore, dyBefore, dxAfter, dyAfter);
        } catch (NullPointerException e) {
            System.out.println("Cannot place track outside world");
        }
    }

}
