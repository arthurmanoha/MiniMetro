package minimetro;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class represents the terrain that contains the tracks, trains,
 * landscape, ...
 *
 * @author arthu
 */
public class World {

    private int nbRows, nbCols;
    private double cellWidth = 1;
    private ArrayList<ArrayList<Cell>> cells;
    private double dt; // Evolution step

    private Timer timer;
    private boolean isRunning;
    private int periodMillisec;
    private int step;

    // Tell observers that our state has changed.
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public World() {
        nbRows = 5;
        nbCols = 9;
        cells = new ArrayList<>();
        double xCell, yCell;
        for (int row = 0; row < nbRows; row++) {
            yCell = cellWidth * row;
            cells.add(new ArrayList<>());
            for (int col = 0; col < nbCols; col++) {
                xCell = cellWidth * col;
                cells.get(row).add(new Cell(xCell, yCell, cellWidth));
            }
        }
        dt = 0.1;
        step = 0;
        isRunning = false;
        periodMillisec = 50;
        startTimer();
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
        isRunning = !isRunning;

        if (isRunning) {
            System.out.println("World: play");
        } else {
            System.out.println("World: pause");
        }
    }

    public void step() {
        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                c.evolve(dt);
            }
        }

        // Transmit trains in transition from the Cells other Cells.
        int rowNumber = 0;
        for (ArrayList<Cell> row : cells) {
            int colNumber = 0;
            for (Cell c : row) {
                for (TrainElement te : c.trainsInTransition) {
                    // Reinserting element in next cell now.
                    reinsertTrain(te, rowNumber, colNumber);
                }
                c.clearTrainsInTransition();
                colNumber++;
            }
            rowNumber++;
        }

        step++;

        updateListeners();
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Notify the listeners that the world's state has changed.
     *
     */
    protected void updateListeners() {
        support.firePropertyChange("currentStep", step - 1, step);
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
            this.setCell(row, col, new Cell(oldCell.xTop, oldCell.yTop, oldCell.width));
        } else {
            this.setCell(row, col, new StationCell(oldCell.xTop, oldCell.yTop, oldCell.width));
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
        c.createNewLoco();
        updateTrainLinks(row, col);
    }

    /**
     * Add a carriage to the end of the train under construction.
     *
     * @param row
     * @param col
     */
    protected void addWagon(int row, int col) {
        Cell c = getCell(row, col);
        c.createNewWagon();
        updateTrainLinks(row, col);
    }

    /**
     * Link the TrainElements in the given cell to its potential neighbors.
     *
     */
    private void updateTrainLinks(int row, int col) {
        // TODO
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

    /**
     * When a train has left its previous cell, it needs to be placed into the
     * next cell.
     *
     */
    private void reinsertTrain(TrainElement movingTrain, int oldRow, int oldCol) {
        int newRow = oldRow;
        int newCol = oldCol;
        if (movingTrain.vx > 0) {
            newCol = oldCol + 1;
            movingTrain.x -= cellWidth;
        } else if (movingTrain.vx < 0) {
            newCol = oldCol - 1;
            movingTrain.x += cellWidth;
        } else if (movingTrain.vy > 0) {
            newRow = oldRow + 1;
            movingTrain.y -= cellWidth;
        } else {
            newRow = oldRow - 1;
            movingTrain.y += cellWidth;
        }
        Cell newCell = cells.get(newRow).get(newCol);
        newCell.addTrainElement(movingTrain);
    }

    /**
     * Compare a heading to another with a 10 degrees margin
     *
     * @return
     */
    private boolean headingIsCloseTo(double h0, int h1) {
        double limit = 11;
        double dh = (h0 - h1) % 360;
        return dh < limit || dh > 360 - limit;
    }

    private void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isRunning) {
                    step();
                }
            }
        }, 0, periodMillisec);
    }

    // Add a rectangular track loop.
    void addTestTracks() {
        int minRow = 1;
        int maxRow = 3;
        int minCol = 1;
        int maxCol = 5;

        // West-East tracks
        for (int col = minCol + 1; col < maxCol; col++) {
            setNewTrack(col - 1, minRow, col, minRow, col + 1, minRow);
            setNewTrack(col - 1, maxRow, col, maxRow, col + 1, maxRow);
        }

        // North-South tracks
        for (int row = minRow + 1; row < maxRow; row++) {
            setNewTrack(minCol, row - 1, minCol, row, minCol, row + 1);
            setNewTrack(maxCol, row - 1, maxCol, row, maxCol, row + 1);
        }
        // Corners
        setNewTrack(minCol, minRow + 1, minCol, minRow, minCol + 1, minRow); // NW
        setNewTrack(maxCol - 1, minRow, maxCol, minRow, maxCol, minRow + 1); // NE
        setNewTrack(minCol, maxRow - 1, minCol, maxRow, minCol + 1, maxRow); // SW
        setNewTrack(maxCol - 1, maxRow, maxCol, maxRow, maxCol, maxRow - 1); // SE
    }

    /**
     * Mark the train in cell0 and that in cell1 as the same train.
     */
    private void addTrainLink(Cell cell0, Cell cell1) {
    }

    /**
     * All TrainElements with current train numbers @old1 or @old2 must be
     * renumbered into @newTrainNumber.
     *
     * @param old1
     * @param old2
     * @param newTrainNumber
     */
    private void renumberTrains(int old1, int old2, int newTrainNumber) {
    }

}
