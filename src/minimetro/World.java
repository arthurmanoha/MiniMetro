package minimetro;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
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
public class World implements PropertyChangeListener {

    private int nbRows, nbCols;
    private ArrayList<ArrayList<Cell>> cells;
    private double dt; // Evolution step

    private ArrayList<TrainElement> trainsInTransition;

    private ArrayList<TrainLink> links;

    private Timer timer;
    private boolean isRunning;
    private int periodMillisec;
    private int step;

    private static int NB_TRAINS_CREATED = 0;

    // Tell observers that our state has changed.
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public World() {
        nbRows = 15;
        nbCols = 24;
        cells = new ArrayList<>();
        for (int row = 0; row < nbRows; row++) {
            double yCell = (nbRows - row - 1) * Cell.cellSize;
            cells.add(new ArrayList<>());
            for (int col = 0; col < nbCols; col++) {
                double xCell = col * Cell.cellSize;
                Point2D.Double newAbsPos = new Point2D.Double(xCell, yCell);
                cells.get(row).add(new Cell(newAbsPos));
            }
        }
        dt = 0.1;
        trainsInTransition = new ArrayList<>();
        links = new ArrayList<>();
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
        // Change speed for locos
        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                c.resetForces();
            }
        }
        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                c.computeMotorForces(dt);
            }
        }

        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                c.computeNewSpeeds(dt);
            }
        }

        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                c.moveTrains(dt);
            }
        }

        // Transfert trains between cells when necessary
        int rowIndex = 0;
        for (ArrayList<Cell> row : cells) {
            int colIndex = 0;
            for (Cell c : row) {
                for (TransferringTrain transferringTrain : c.trainsLeavingCell) {
                    TrainElement movingTrain = transferringTrain.getTrainElement();
                    CardinalPoint direction = transferringTrain.getDirection();
                    int newRow = rowIndex, newCol = colIndex;
                    switch (direction) {
                    case NORTH:
                        newRow = rowIndex - 1;
                        break;
                    case EAST:
                        newCol = colIndex + 1;
                        break;
                    case SOUTH:
                        newRow = rowIndex + 1;
                        break;
                    case WEST:
                        newCol = colIndex - 1;
                        break;
                    }
                    reinsertTrain(movingTrain, newRow, newCol);
                }
                colIndex++;
            }
            rowIndex++;
        }

        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                c.snapToRail();
            }
        }

        // Flush transfer lists
        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                c.flushMovingTrains();
            }
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
     * Create a link between the specified cell and its specified neighbor.
     *
     * @param row the row of the specified cell
     * @param col the column of the specified cell
     * @param rowNeighbor the row of the other cell
     * @param colNeighbor the column of the other cell
     */
    protected void setNewTrack(int row, int col, int rowNeighbor, int colNeighbor) {

        Cell newTrackCell = getCell(rowNeighbor, colNeighbor);
        CardinalPoint newLinkDirection;

        if (newTrackCell != null) {
            if (col < colNeighbor) {
                if (row < rowNeighbor) {
                    newLinkDirection = CardinalPoint.NORTHWEST;
                } else if (row == rowNeighbor) {
                    newLinkDirection = CardinalPoint.WEST;
                } else {
                    newLinkDirection = CardinalPoint.SOUTHWEST;
                }
            } else if (col == colNeighbor) {
                if (row < rowNeighbor) {
                    newLinkDirection = CardinalPoint.NORTH;
                } else {
                    newLinkDirection = CardinalPoint.SOUTH;
                }
            } else {
                if (row < rowNeighbor) {
                    newLinkDirection = CardinalPoint.NORTHEAST;
                } else if (row == rowNeighbor) {
                    newLinkDirection = CardinalPoint.EAST;
                } else {
                    newLinkDirection = CardinalPoint.SOUTHEAST;
                }
            }
            newTrackCell.addLink(newLinkDirection);
        }
    }

    /**
     * When a train has left its previous cell, it needs to be placed into the
     * next cell (specified with rowIndex, colIndex).
     *
     */
    private void reinsertTrain(TrainElement movingTrain, int rowIndex, int colIndex) {
        double currentHeading = movingTrain.headingDegrees;
        if (headingIsCloseTo(currentHeading, 0)) {
            // North
            movingTrain.setHeadingDegrees(0);
        } else if (headingIsCloseTo(currentHeading, 90)) {
            // East
            movingTrain.setHeadingDegrees(90);
        } else if (headingIsCloseTo(currentHeading, 180)) {
            // South
            movingTrain.setHeadingDegrees(180);
        } else if (headingIsCloseTo(currentHeading, 270)) {
            // West
            movingTrain.setHeadingDegrees(270);
        } else {
            System.out.println("World: error, direction unknown.");
        }

        // Add the train to the new cell.
        Cell newCell = this.getCell(rowIndex, colIndex);
        if (newCell == null) {
            System.out.println("World: error, next cell is null");
        } else {
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
        int minRow = 7;
        int maxRow = 10;
        int minCol = 2;
        int maxCol = 6;

        // West-East tracks
        for (int col = minCol; col < maxCol; col++) {
            setNewTrack(minRow, col, minRow, col + 1);
            setNewTrack(minRow, col + 1, minRow, col);
            setNewTrack(maxRow, col, maxRow, col + 1);
            setNewTrack(maxRow, col + 1, maxRow, col);
        }

        // North-South tracks
        for (int row = minRow; row < maxRow; row++) {
            setNewTrack(row, minCol, row + 1, minCol);
            setNewTrack(row + 1, minCol, row, minCol);
            setNewTrack(row, maxCol, row + 1, maxCol);
            setNewTrack(row + 1, maxCol, row, maxCol);
        }
    }

    /**
     * Mark the train in cell0 and that in cell1 as the same train.
     */
    private void addTrainLink(Cell cell0, Cell cell1) {
        System.out.println("adding train link between cell with heading " + (int) cell0.getCenterHeading()
                + " and cell with heading " + (int) cell1.getCenterHeading());

        if (cell0.hasTrain() && cell1.hasTrain()) {

            TrainElement elem0 = cell0.getTrainElement();
            TrainElement elem1 = cell1.getTrainElement();

            if (elem0 != null && elem1 != null) {
                int trainNumber0 = elem0.trainNumber;
                int trainNumber1 = elem1.trainNumber;

                // All elements from these two trains must be marked as the same one.
                int newTrainNumber = NB_TRAINS_CREATED;
                NB_TRAINS_CREATED++;
                // Update the two elements that are being linked
                elem0.trainNumber = newTrainNumber;
                elem1.trainNumber = newTrainNumber;
                System.out.println("            new train number: " + elem0.trainNumber);
                // Update all the other elements of the same train
                renumberTrains(trainNumber0, trainNumber1, newTrainNumber);

                double angleDiff = (elem1.headingDegrees - elem0.headingDegrees) % 360;
                double newHeading;
                // When the headings are mode than 45 degrees apart:
                if (angleDiff > 46 && angleDiff < 314) {
                    System.out.println("Too large angle diff");
                    if (trainNumber0 < trainNumber1) {
                        // elem1 adapts its direction to elem0
                        newHeading = (elem1.headingDegrees + 180) % 360;
                        System.out.println("    Switching elem0 from " + (int) elem1.headingDegrees + " to " + (int) newHeading);
                        elem1.headingDegrees = newHeading;
                    } else {
                        // elem0 adapts its direction to elem1
                        newHeading = (elem0.headingDegrees + 180) % 360;
                        System.out.println("    Switching elem0 from " + (int) elem0.headingDegrees + " to " + (int) newHeading);
                        elem0.headingDegrees = newHeading;
                    }
                }
                double distance = computeDistance(elem0, cell0, elem1, cell1);
                System.out.println("    new link, distance = " + distance);
                links.add(new TrainLink(elem0, elem1, distance));
                System.out.println("End link.");
            }
        } else {
            System.out.println("No link to be created.");
        }
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
        System.out.println("    renumbering trains " + old1 + " and " + old2 + " into " + newTrainNumber);
        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {

                c.renumberTrainElement(old1, newTrainNumber);
                c.renumberTrainElement(old2, newTrainNumber);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
//        System.out.println("World received event: " + evt);
    }

    /**
     * Compute the distance between the elements, in absolute coordinates.
     *
     * @param elem0
     * @param cell0
     * @param elem1
     * @param cell1
     * @return
     */
    private double computeDistance(TrainElement elem0, Cell cell0, TrainElement elem1, Cell cell1) {

        int col0 = getColumn(cell0);
        int row0 = getLine(cell0);
        int col1 = getColumn(cell1);
        int row1 = getLine(cell1);

        System.out.println("Computing distance: col " + col0 + ", line " + row0
                + " and col " + col1 + ", line " + row1);

        Point2D.Double pos0 = cell0.getTrainPosition(elem0);
        Point2D.Double pos1 = cell1.getTrainPosition(elem1);

        double x0 = col0 + pos0.x;
        double y0 = row0 + pos0.y;
        double x1 = col1 + pos1.x;
        double y1 = row1 + pos1.y;
        double dx = x0 - x1;
        double dy = y0 - y1;

        double distance = Math.sqrt(dx * dx + dy * dy);
        System.out.println("distance : " + distance);
        return distance;
    }

    private int getColumn(Cell cell0) {
        for (int row = 0; row < nbRows; row++) {
            for (int col = 0; col < nbCols; col++) {
                if (getCell(row, col).equals(cell0)) {
                    return col;
                }
            }
        }
        return -1;
    }

    private int getLine(Cell cell0) {
        for (int row = 0; row < nbRows; row++) {
            for (int col = 0; col < nbCols; col++) {
                if (getCell(row, col).equals(cell0)) {
                    return row;
                }
            }
        }
        return -1;
    }

//    /**
//     * Apply an acceleration to both elements that share this link.
//     *
//     * @param link
//     */
//    private void applyLinkForce(TrainLink link) {
//        TrainElement e0 = link.getElement(0);
//        TrainElement e1 = link.getElement(1);
//
//        if (e0 != null && e1 != null) {
//
//            Cell c0 = getCell(e0);
//            Cell c1 = getCell(e1);
//
//            System.out.println("Applying link force between cells " + getLine(c0) + ", " + getColumn(c0)
//                    + " and " + getLine(c1) + ", " + getColumn(c1));
//
//            Point2D.Double posC0 = getCellPosition(c0);
//            if (posC0 == null) {
//                System.out.println("pos c0 is null");
//            }
//            Point2D.Double posC1 = getCellPosition(c1);
//            if (posC1 == null) {
//                System.out.println("pos c1 is null");
//            }
//
//            Point2D.Double posElem0 = c0.getTrainPosition(e0);
//            if (posElem0 == null) {
//                System.out.println("pos elem0 is null");
//            }
//            Point2D.Double posElem1 = c1.getTrainPosition(e1);
//            if (posElem1 == null) {
//                System.out.println("pos elem1 is null");
//            }
//
//            Point2D.Double force = link.computeForce(posC0, posC1, posElem0, posElem1);
//
//            c0.applyForce(force, true, e0);
//            c1.applyForce(force, false, e1);
//        }
//    }
    /**
     * Find and return the cell that contains the given element, or null if no
     * cell contains it.
     *
     * @param e0
     * @return
     */
    private Cell getCell(TrainElement e0) {
        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                if (c.containsTrainElement(e0)) {
                    return c;
                }
            }
        }
        return null;
    }

    private Point2D.Double getCellPosition(Cell c) {
        int row = getLine(c);
        int col = getColumn(c);
        return new Point2D.Double(col, row);
    }
}
