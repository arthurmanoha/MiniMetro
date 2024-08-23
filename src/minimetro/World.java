package minimetro;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;

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

    protected ArrayList<TrainLink> links;

    private Timer timer;
    private boolean isRunning;
    private int periodMillisec;
    private int step;

    private static int NB_TRAINS_CREATED = 0;

    // Maximum distance between TEs for a link to be created.
    private static double distanceMax = 0.15;

    // Tell observers that our state has changed.
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public World() {
        nbRows = 40;
        nbCols = 100;
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
        periodMillisec = 30;
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
                c.resetForces();
            }
        }

        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                c.computeMotorForces(dt); // Set the force applied on each loco
            }
        }

        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                c.snapToRail();
            }
        }
        applyLinkForces();

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
//        } else {
//            this.setCell(row, col, new StationCell());
        }
    }

    /**
     * Add a locomotive
     *
     * @param xReal
     * @param yReal
     */
    protected void addLoco(double xReal, double yReal) {
        this.addTrainElement(xReal, yReal, true);
    }

    /**
     * Add a wagon
     *
     * @param xReal
     * @param yReal
     */
    protected void addWagon(double xReal, double yReal) {
        this.addTrainElement(xReal, yReal, false);
    }

    /**
     * Add a loco or wagon at a specific location within a cell.
     *
     * @param xReal
     * @param yReal
     * @param isLoco when true, add a loco, otherwise add a wagon
     */
    private void addTrainElement(double xReal, double yReal, boolean isLoco) {

        double size = Cell.cellSize;
        int col = (int) ((xReal + size / 2) / size);
        int row = nbRows - (int) ((yReal + size / 2) / size) - 1;

        Point2D.Double newAbsolutePosition = new Point2D.Double(xReal, yReal);

        TrainElement newElement;
        if (isLoco) {
            newElement = new Locomotive(newAbsolutePosition);
        } else {
            newElement = new Wagon(newAbsolutePosition);
        }

        Cell cell = getCell(row, col);
        if (cell != null && cell.hasRails()) {
            cell.addTrainElement(newElement);
            updateTrainLinks(newElement, row, col);
        }
    }

    /**
     * Link the TrainElements in the given cell to their closest neighbors.
     *
     */
    private void updateTrainLinks(TrainElement te, int row, int col) {

        // Find all element that could share a potential link with @te
        // The list allElementsNearby contains all elements in this cell or in a neighboring cell.
        ArrayList<TrainElement> allElementNearby = new ArrayList<>();
        for (int currentRow = row - 1; currentRow <= row + 1; currentRow++) {
            for (int currentCol = col - 1; currentCol <= col + 1; currentCol++) {
                Cell neighborCell = getCell(currentRow, currentCol);
                if (neighborCell != null) {
                    allElementNearby.addAll(neighborCell.getAllElements());
                }
            }
        }
        allElementNearby.remove(te);

        for (TrainElement neighbor : allElementNearby) {
            double currentDistance = computeDistance(te, neighbor);
            if (currentDistance < distanceMax * 1.5) {
                if (getNbLinks(neighbor) < 2) {
                    links.add(new TrainLink(te, neighbor, distanceMax));
                }
            }
        }
    }

    /**
     * Return the number of links that involve the given element.
     *
     * @param e
     * @return
     */
    private int getNbLinks(TrainElement e) {
        int result = 0;
        for (TrainLink link : links) {
            if (link.getElement(0).equals(e) || link.getElement(1).equals(e)) {
                // This link is related to the parameter element.
                result++;
            }
        }
        return result;
    }

    /**
     * Create a link in the specified cell headed to the specified neighbor.
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
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            step();
                        }
                    });
                }
            }
        }, 0, periodMillisec);
    }

    // Add a rectangular track loop.
    protected void addTestTracks(int minRow, int maxRow, int minCol, int maxCol) {

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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
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
    private double computeDistance(TrainElement elem0, TrainElement elem1) {

        Point2D.Double pos0 = elem0.absolutePosition;
        Point2D.Double pos1 = elem1.absolutePosition;

        double x0 = pos0.x;
        double y0 = pos0.y;
        double x1 = pos1.x;
        double y1 = pos1.y;
        double dx = x0 - x1;
        double dy = y0 - y1;

        double distance = Math.sqrt(dx * dx + dy * dy);
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

    private void applyLinkForces() {
        for (TrainLink link : links) {
            applyLinkForce(link);
        }
    }

    /**
     * Apply an acceleration to both elements that share this link.
     *
     * @param link
     */
    private void applyLinkForce(TrainLink link) {
        TrainElement e0 = link.getElement(0);
        TrainElement e1 = link.getElement(1);

        if (e0 != null && e1 != null) {

            Cell c0 = getCell(e0);
            Cell c1 = getCell(e1);

            Point2D.Double posC0 = getCellPosition(c0);
            if (posC0 == null) {
                System.out.println("pos c0 is null");
            }
            Point2D.Double posC1 = getCellPosition(c1);
            if (posC1 == null) {
                System.out.println("pos c1 is null");
            }

            Point2D.Double posElem0 = c0.getTrainPosition(e0);
            if (posElem0 == null) {
                System.out.println("pos elem0 is null");
            }
            Point2D.Double posElem1 = c1.getTrainPosition(e1);
            if (posElem1 == null) {
                System.out.println("pos elem1 is null");
            }

            Point2D.Double force = link.computeForce(posElem0, posElem1);
            Point2D.Double forceOpposite = new Point2D.Double(-force.x, -force.y);

            e0.increaseEfficientForce(force);
            e1.increaseEfficientForce(forceOpposite);
        }
    }

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

    protected void removeTrack(int row, int col) {
        Cell c = getCell(row, col);
        c.removeTracks();
    }

    protected void removeTrains(int row, int col) {
        Cell c = getCell(row, col);
        ArrayList<TrainElement> list = c.getAllElements();
        // Before we remove elem, we must remove any TrainLink involved.
        for (TrainElement elem : list) {
            Iterator<TrainLink> iterator = links.iterator();
            while (iterator.hasNext()) {
                TrainLink link = iterator.next();
                if (link.getElement(0).equals(elem) || link.getElement(1).equals(elem)) {
                    iterator.remove();
                }
            }
        }
        c.removeTrains();
    }

    protected void addTestTrain(double xStart, double yStart, int nbWagons) {

        double spacing = distanceMax;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                addLoco(xStart, yStart);
                for (int i = 1; i <= nbWagons; i++) {
                    addWagon(xStart - spacing * i, yStart);
                }
                updateListeners();
            }
        });
    }
}
