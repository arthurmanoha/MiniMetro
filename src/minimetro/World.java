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
    private ArrayList<ArrayList<Cell>> cells;
    private double dt; // Evolution step

    private ArrayList<TrainElement> trainsInTransition;

    private Timer timer;
    private boolean isRunning;
    private int periodMillisec;
    private int step;

    // Tell observers that our state has changed.
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public World() {
        nbRows = 150;
        nbCols = 240;
        cells = new ArrayList<>();
        for (int row = 0; row < nbRows; row++) {
            cells.add(new ArrayList<>());
            for (int col = 0; col < nbCols; col++) {
                cells.get(row).add(new Cell());
            }
        }
        dt = 0.1;
        trainsInTransition = new ArrayList<>();
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

        // Transfert trains between cells when necessary
        int rowIndex = 0;
        for (ArrayList<Cell> row : cells) {
            int colIndex = 0;
            for (Cell c : row) {
                if (c.isTrainElementSwitchingCells) {
                    TrainElement movingTrain = c.removeTrain();
                    reinsertTrain(movingTrain, rowIndex, colIndex);
                    c.isTrainElementSwitchingCells = false;
                }
                colIndex++;
            }
            rowIndex++;
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
        c.setWagon();
        updateTrainLinks(row, col);
    }

    /**
     * Link the TrainElements in the given cell to its potential neighbors.
     *
     */
    private void updateTrainLinks(int row, int col) {

        Cell currentCell = getCell(row, col);
        Cell neighbor;
        if (currentCell.hasLinkTowardNeighbor(0)) {
            // Test the North cell
            neighbor = getCell(row - 1, col);
            if (neighbor.hasLinkTowardNeighbor(180)) {
                addTrainLink(currentCell, neighbor);
            }
        }
        if (currentCell.hasLinkTowardNeighbor(90)) {
            // Test the East cell
            neighbor = getCell(row, col + 1);
            if (neighbor.hasLinkTowardNeighbor(270)) {
                addTrainLink(currentCell, neighbor);
            }
        }
        if (currentCell.hasLinkTowardNeighbor(180)) {
            // Test the East cell
            neighbor = getCell(row + 1, col);
            if (neighbor.hasLinkTowardNeighbor(0)) {
                addTrainLink(currentCell, neighbor);
            }
        }
        if (currentCell.hasLinkTowardNeighbor(270)) {
            // Test the East cell
            neighbor = getCell(row, col - 1);
            if (neighbor.hasLinkTowardNeighbor(90)) {
                addTrainLink(currentCell, neighbor);
            }
        }
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
    private void reinsertTrain(TrainElement movingTrain, int rowIndex, int colIndex) {
        double currentHeading = movingTrain.headingDegrees;
        int newRow = rowIndex;
        int newCol = colIndex;
        if (headingIsCloseTo(currentHeading, 0)) {
            // North
            newRow--;
            movingTrain.setHeadingDegrees(0);
        } else if (headingIsCloseTo(currentHeading, 90)) {
            // East
            newCol++;
            movingTrain.setHeadingDegrees(90);
        } else if (headingIsCloseTo(currentHeading, 180)) {
            // South
            newRow++;
            movingTrain.setHeadingDegrees(180);
        } else if (headingIsCloseTo(currentHeading, 270)) {
            // West
            newCol--;
            movingTrain.setHeadingDegrees(270);
        } else {
            System.out.println("World: direction unknown.");
        }

        // Add the train to the new cell.
        Cell newCell = this.getCell(newRow, newCol);
        if (newCell == null) {
            System.out.println("next cell is null");
        } else {
            movingTrain.increasePosition(-1); // Adapt the train's current position to the new cell.
            TrainElement insertionCheck = newCell.addTrainElement(movingTrain);
            if (insertionCheck != null) {
                // Error in train reinsertion.
                System.out.println("World: error in train reinsertion.");
            }
        }
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
        int minRow = 2;
        int maxRow = 5;
        int minCol = 2;
        int maxCol = 6;

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
        setNewTrack(minCol, minRow + 1, minCol, minRow, minCol + 1, maxRow); // NW
        setNewTrack(maxCol, minRow + 1, maxCol, minRow, maxCol - 1, maxRow); // NE
        setNewTrack(minCol, maxRow - 1, minCol, maxRow, minCol + 1, maxRow); // SW
        setNewTrack(maxCol, maxRow - 1, maxCol, maxRow, maxCol - 1, maxRow); // SE
    }

    /**
     * Mark the train in cell0 and that in cell1 as the same train.
     */
    private void addTrainLink(Cell cell0, Cell cell1) {
        System.out.println("adding train link between cell with heading " + cell0.getCenterHeading()
                + " and cell with heading " + cell1.getCenterHeading());

        TrainElement elem0 = cell0.getTrainElement();
        TrainElement elem1 = cell1.getTrainElement();

        if (elem0 != null && elem1 != null) {
            int trainNumber0 = elem0.trainNumber;
            int trainNumber1 = elem1.trainNumber;

            // All elements from these two trains must be marked as the same one.
            int newTrainNumber = Math.min(trainNumber0, trainNumber1);
            renumberTrains(trainNumber0, trainNumber1, newTrainNumber);

            double angleDiff = (elem1.headingDegrees - elem0.headingDegrees) % 360;
            double newHeading;
            // When the headings are mode than 45 degrees apart:
            if (angleDiff > 46 && angleDiff < 314) {
                System.out.println("Too large angle diff");
                if (trainNumber0 < trainNumber1) {
                    // elem1 adapts its direction to elem0
                    newHeading = (elem1.headingDegrees + 180) % 360;
                    System.out.println("    Switching elem0 from " + elem1.headingDegrees + " to " + newHeading);
                    elem1.headingDegrees = newHeading;
                } else {
                    // elem0 adapts its direction to elem1
                    newHeading = (elem0.headingDegrees + 180) % 360;
                    System.out.println("    Switching elem0 from " + elem0.headingDegrees + " to " + newHeading);
                    elem0.headingDegrees = newHeading;
                }
            }
        }
        System.out.println("End link.");
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

        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {

                c.renumberTrainElement(old1, newTrainNumber);
                c.renumberTrainElement(old2, newTrainNumber);
            }
        }
    }

}
