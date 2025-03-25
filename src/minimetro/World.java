package minimetro;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.floor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import javax.swing.Timer;
import static minimetro.CardinalPoint.*;

/**
 * This class represents the terrain that contains the tracks, trains,
 * landscape, ...
 *
 * @author arthu
 */
public class World {

    private static final String RAIL_LINK = "rail_link";
    private static final String SWITCH = "switch";
    private static final String STATION = "station";
    private static final String SPEED_LIMIT = "speed_limit";
    private static final String STOP_TIMER = "stop_timer";
    public static final String LOCOMOTIVE = "Locomotive";
    public static final String WAGON = "Wagon";
    public static final String PASSENGER = "Passenger";
    public static final String ONBOARD = "Onboard";
    public static final String YES = "yes";
    public static final String NO = "no";

    private double lowSpeedLimit = 2;
    private double mediumSpeedLimit = 10;
    private double highSpeedLimit = 50;

    private int nbRows, nbCols;
    protected Cell cells[][];
    protected ArrayList<Cell> activeCells;
    private ArrayList<Cell> newlyActiveCells;
    private double simulationDt; // Time elapsed in world during one simulation step.

    private PerlinNoise noiseGenerator;
    int perlinCellSize;
    double perlinScale;

    // Links between TrainElements;
    protected ArrayList<TrainLink> links;

    private boolean isSettingLongDistanceTracks;
    private int longTrackStartRow;
    private int longTrackStartCol;

    private Timer timer;
    private boolean isRunning;
    private int periodMillisec; // Time elapsed in real world between two simulation steps.
    private int step;

    // Maximum distance between TEs for a link to be created.
    private static double distanceMax = 5;

    // Tell observers that our state has changed.
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private double speedIndicatorValue;
    private double stopTimerValue;

    public static WorldMap map;
    private ArrayList<StationCell> stationList;

    public World() {
        this(600, 600);
    }

    public World(int newNbRows, int newNbCols) {
        nbRows = newNbRows;
        nbCols = newNbCols;

        perlinCellSize = 16;
        perlinScale = perlinCellSize * Cell.cellSize;
        noiseGenerator = new PerlinNoise(perlinScale);

        initializeGrid();
        isSettingLongDistanceTracks = false;
    }

    /**
     * Create a new cell at the selected row and col.
     *
     * @param row
     * @param col
     */
    private void addNewCell(int row, int col) {
        addNewCell(null, row, col);
    }

    /**
     * Add the specified new cell at the specified position, or create a new
     * cell there.
     *
     * @param newCell
     * @param row
     * @param col
     */
    protected void addNewCell(Cell newCell, int row, int col) {
        try {
            double xCell = col * Cell.cellSize;
            double yCell = (nbRows - row - 1) * Cell.cellSize;
            Point2D.Double newAbsPos = new Point2D.Double(xCell, yCell);
            if (newCell == null) {
                newCell = new Cell(newAbsPos);
            } else {
                newCell.absolutePosition = newAbsPos;
            }
            newCell.setRow(row);
            newCell.setCol(col);
            cells[row][col] = newCell;
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private void initializeGrid() {
        System.out.println("World.initializeGrid()");
        cells = new Cell[nbRows][];
        for (int row = 0; row < nbRows; row++) {
            cells[row] = new Cell[nbCols];
            for (int col = 0; col < nbCols; col++) {
                Cell newCell = new Cell(row, col);
                newCell.altitude = getAltitude(col, row);
                newCell.absolutePosition = new Point2D.Double(col * Cell.cellSize, (nbRows - row - 1) * Cell.cellSize);
                cells[row][col] = newCell;
                newCell.setRow(row);
                newCell.setCol(col);
            }
        }
        System.out.println("End cells initialization");
        activeCells = new ArrayList<>();
        newlyActiveCells = new ArrayList<>();
        links = new ArrayList<>();
        step = 0;
        isRunning = false;
        simulationDt = 0.03;
        periodMillisec = 20;
        startTimer();
        speedIndicatorValue = 0;
        stopTimerValue = 5;
        map = new WorldMap(this);
        stationList = new ArrayList<>();
    }

    public int getNbRows() {
        return nbRows;
    }

    public int getNbCols() {
        return nbCols;
    }

    /**
     * Retrieve the cell at specified row and col, or null if no such cell
     * exists.
     *
     * @param row
     * @param col
     * @return
     */
    public Cell getCell(int row, int col) {
        try {
            return cells[row][col];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Retrieve the cell at specified row and col, and create it if it does not
     * exist.
     *
     * @param row
     * @param col
     * @return
     */
    private Cell getCellOrCreateIfNull(int row, int col) {
        Cell c = getCell(row, col);
        if (c == null) {
            addNewCell(row, col);
        }
        return getCell(row, col);
    }

    /**
     * Get all the cells in the form of an ArrayList.
     *
     * @return a list containing all the cells.
     */
    protected ArrayList<Cell> getAllCells() {
        ArrayList<Cell> allCells = new ArrayList<>();
        for (int row = 0; row < nbRows; row++) {
            for (int col = 0; col < nbCols; col++) {
                allCells.add(cells[row][col]);
            }
        }
        return allCells;
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

        for (Cell c : activeCells) {
            c.resetForces();
        }

        for (Cell c : activeCells) {
            c.computeMotorForces(simulationDt); // Set the force applied on each loco
        }

        for (Cell c : activeCells) {
            c.snapToRail();
        }

        applyLinkForces();

        for (Cell c : activeCells) {
            c.computeNewSpeeds(simulationDt);
        }

        for (Cell c : activeCells) {
            c.moveTrains(simulationDt);
        }

        for (Cell c : activeCells) {
            c.movePassengers(simulationDt);
        }

        getPassengersOff();
        boardPassengers();
        reinsertMovingPassengers();
        transferTrainsBetweenCells();

        // Update paths for passengers in stations or stopped trains
        for (Cell c : getAllCells()) {
            if (c instanceof StationCell) {
                StationCell station = (StationCell) c;
                for (Passenger p : station.passengerList) {
                    World.map.computePath(station.getId(), p);
                }
                for (TrainElement te : c.getAllElements()) {
                    if (te instanceof Wagon && te.isStopped()) {
                        Wagon wagon = (Wagon) te;
                        for (Passenger p : wagon.getPassengers()) {
                            World.map.computePath(station.getId(), p);
                        }
                    }
                }
            }
        }

        // Flush transfer lists
        for (Cell c : activeCells) {
            c.flushMovingTrains();
        }

        for (Cell c : newlyActiveCells) {
            if (!activeCells.contains(c)) {
                activeCells.add(c);
            }
        }
        newlyActiveCells.clear();

        Iterator iter = activeCells.iterator();
        while (iter.hasNext()) {
            Cell c = (Cell) iter.next();
            if (!c.hasTrain() && !c.hasPassengers()) {
                iter.remove();
                c.setActive(false);
            }
        }

        step++;

        updateListeners();
    }

    private void reinsertMovingPassengers() {
        // Reinsert moving passengers
        for (Cell c : activeCells) {
            int row = c.getRow();
            int col = c.getCol();

            if (c instanceof StationCell) {
                StationCell station = (StationCell) c;
                for (Passenger p : station.passengersLeavingCell) {
                    int newRow = row;
                    int newCol = col;

                    double xLocal = p.getX() - ((StationCell) c).absolutePosition.x;
                    double yLocal = p.getY() - ((StationCell) c).absolutePosition.y;
                    // Which neighbor the passenger is walking to depends on which quadrant he is.
                    if (xLocal > yLocal) {
                        // South or East
                        if (xLocal > -yLocal) {
                            // Reinsert East
                            newCol = col + 1;
                        } else {
                            // Reinsert South
                            newRow = row + 1;
                        }
                    } else {
                        // West or North
                        if (xLocal > -yLocal) {
                            // Reinsert North
                            newRow = row - 1;
                        } else {
                            // Reinsert West
                            newCol = col - 1;
                        }
                    }
                    Cell newCell = getCell(newRow, newCol);
                    if (newCell instanceof StationCell) {
                        ((StationCell) newCell).addPassenger(p);
                        if (!activeCells.contains(newCell)) {
                            newlyActiveCells.add(newCell);
                            newCell.setActive(true);
                        }
                    }
                }
                station.flushMovingPassengers();
            }
        }
    }

    private void transferTrainsBetweenCells() {
        // Transfert trains between cells when necessary

        for (Cell c : activeCells) {
            int rowIndex = c.getRow();
            int colIndex = c.getCol();
            if (rowIndex != Integer.MIN_VALUE) {
                for (TransferringTrain transferringTrain : c.getTrainsLeavingCell()) {
                    TrainElement movingTrain = transferringTrain.getTrainElement();
                    CardinalPoint direction = transferringTrain.getDirection();
                    int newRow = rowIndex, newCol = colIndex;
                    switch (direction) {
                    case NORTH:
                        newRow = rowIndex - 1;
                        break;
                    case NORTHEAST:
                        newRow = rowIndex - 1;
                        newCol = colIndex + 1;
                        break;
                    case EAST:
                        newCol = colIndex + 1;
                        break;
                    case SOUTHEAST:
                        newRow = rowIndex + 1;
                        newCol = colIndex + 1;
                        break;
                    case SOUTH:
                        newRow = rowIndex + 1;
                        break;
                    case SOUTHWEST:
                        newRow = rowIndex + 1;
                        newCol = colIndex - 1;
                        break;
                    case WEST:
                        newCol = colIndex - 1;
                        break;
                    case NORTHWEST:
                        newRow = rowIndex - 1;
                        newCol = colIndex - 1;
                        break;
                    }
                    reinsertTrain(movingTrain, newRow, newCol);
                }
            }
        }
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
        cells[row][col] = newCell;
    }

    /**
     * Convert a simple cell into a station, and the other way around.
     *
     * @param row
     * @param col
     * @param newStationId
     */
    protected void toggleStation(int row, int col, int newStationId) {
        System.out.println("station row " + row + ", col " + col);
        Cell oldCell = this.getCellOrCreateIfNull(row, col);
        if (oldCell != null) {
            Cell newCell;

            ArrayList<TrainElement> oldTrains = oldCell.getAllElements();

            if (oldCell instanceof StationCell) {
                // Remove a station
                newCell = new Cell(oldCell);
                stationList.remove((StationCell) oldCell);
                setCell(row, col, newCell);
            } else {
                // Create a station
                newCell = new StationCell(oldCell, newStationId);
                stationList.add((StationCell) newCell);
            }
            if (!newCell.isEmpty()) {
                this.setCell(row, col, newCell);
                for (TrainElement oldTrain : oldTrains) {
                    newCell.addTrainElement(oldTrain);
                }
            }
            activeCells.remove(oldCell);
            if (newCell.hasTrain() || newCell.hasPassengers()) {
                activeCells.add(newCell);
            }
        }
    }

    protected void toggleStation(int row, int col) {
        toggleStation(row, col, -1);
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
     * Add a locomotive with a specified heading
     *
     * @param xReal
     * @param yReal
     * @param headingDegrees
     * @param linearSpeed
     */
    protected void addLoco(int id, double xReal, double yReal, double headingDegrees,
            double linearSpeed, boolean isEngineActive, boolean isBraking, double currentSpeedLimit) {
        this.addTrainElement(id, xReal, yReal, headingDegrees, linearSpeed, true, isEngineActive, isBraking, currentSpeedLimit);
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
     * Add a wagon with a specified heading
     *
     * @param xReal
     * @param yReal
     * @param headingDegrees
     */
    protected Wagon addWagon(int id, double xReal, double yReal, double headingDegrees, double linearSpeed) {
        return (Wagon) this.addTrainElement(id, xReal, yReal, headingDegrees, linearSpeed, false, false, false, -1);
    }

    private void addTrainElement(double xReal, double yReal, boolean isLoco) {
        this.addTrainElement(-1, xReal, yReal, 0, 0, isLoco, false, false, -1);
    }

    /**
     * Add a loco or wagon at a specific location within a cell.
     *
     * @param xReal
     * @param yReal
     * @param isLoco when true, add a loco, otherwise add a wagon
     */
    private TrainElement addTrainElement(int id, double xReal, double yReal, double headingDegrees, double linearSpeed,
            boolean isLoco, boolean isEngineActive, boolean isBraking, double currentSpeedLimit) {

        int col = getCol(xReal);
        int row = getRow(yReal);

        Point2D.Double newAbsolutePosition = new Point2D.Double(xReal, yReal);

        TrainElement newElement;
        if (isLoco) {
            newElement = new Locomotive(id, newAbsolutePosition);
        } else {
            newElement = new Wagon(id, newAbsolutePosition);
        }

        if (isEngineActive) {
            newElement.isEngineActive = isEngineActive;
            newElement.isBraking = isBraking;
        }

        newElement.setHeadingDegrees(headingDegrees);
        newElement.setLinearSpeed(linearSpeed);
        newElement.setSpeedLimit(currentSpeedLimit);

        Cell cell = getCell(row, col);
        if (cell != null && cell.hasRails()) {
            cell.addTrainElement(newElement);
            if (!activeCells.contains(cell)) {
                activeCells.add(cell);
                cell.setActive(true);
            }
            updateTrainLinks(newElement, row, col);
        }
        return newElement;
    }

    /**
     * Tell which column a given coordinate belongs to.
     *
     * @param xReal
     * @return
     */
    private int getCol(double xReal) {
        int result = (int) ((xReal + Cell.cellSize / 2) / Cell.cellSize);
        return result;
    }

    private int getRow(double yReal) {
        int result = nbRows - (int) ((yReal + Cell.cellSize / 2) / Cell.cellSize) - 1;
        return result;
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

        Cell newTrackCell = getCellOrCreateIfNull(rowNeighbor, colNeighbor);
        CardinalPoint newLinkDirection;

        if (newTrackCell != null && !newTrackCell.hasTrain()) {
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

        if (rowIndex != Integer.MAX_VALUE) {

            // Add the train to the new cell.
            Cell newCell = this.getCellOrCreateIfNull(rowIndex, colIndex);
            if (newCell == null) {
                System.out.println("World: error, next cell is null");
            } else {
                newCell.addTrainElement(movingTrain);
                newCell.setActive(true);
                newlyActiveCells.add(newCell);
            }
        }
    }

    private void startTimer() {

        if (timer != null && timer.isRunning()) {
            System.out.println("Stopping timer.");
            timer.stop();
        }

        System.out.println("Starting timer.");

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isRunning) {
                    step();
                }
            }
        };
        timer = new Timer(periodMillisec, listener);
        timer.start();
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
        TrainElement e0 = null, e1 = null;
        try {
            e0 = link.getElement(0);
            e1 = link.getElement(1);

            if (e0 != null && e1 != null) {

                Cell c0 = getCell(e0.row, e0.col);
                Cell c1 = getCell(e1.row, e1.col);

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
        } catch (NullPointerException e) {
            System.out.println("NPE in World.applyLinkForce "
                    + ", e0: " + e0 + ", e1: " + e1);
        }
    }

    private Point2D.Double getCellPosition(Cell c) {
        int row = c.getRow();
        int col = c.getCol();
        return new Point2D.Double(col, row);
    }

    protected void removeTrack(int row, int col) {
        Cell c = getCell(row, col);
        if (c != null) {
            if (!c.hasTrain()) {
                if (c instanceof SwitchCell) {
                    Cell newCell = new Cell(c);
                    newCell.setRow(((SwitchCell) c).row);
                    newCell.setCol(((SwitchCell) c).col);
                    setCell(row, col, newCell);
                } else {
                    c.removeTracksAndLinks();
                    c.removeTracksAndLinks();
                }
            }
        }
    }

    protected void removeTrains(int row, int col) {
        Cell c = getCell(row, col);
        if (c != null) {
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
            if (!c.hasTrain()) {
                activeCells.remove(c);
            }
        }
    }

    protected void startLocos() {
        for (Cell c : getAllCells()) {
            c.startLocos();
        }
    }

    protected void stopLocos() {
        for (Cell c : getAllCells()) {
            c.stopLocos();
        }
    }

    protected void setSpeedLimitValue(double newSpeedLimit) {
        speedIndicatorValue = newSpeedLimit;
    }

    protected void setSpeedIndicator(int row, int col) {
        Cell c = getCell(row, col);
        if (c != null) {
            c.setSpeedIndicator(speedIndicatorValue);
        }
    }

    protected void setStopTimerDuration(double newStopTimerDuration) {
        stopTimerValue = newStopTimerDuration;
    }

    protected void setStopTimer(int row, int col) {
        Cell c = getCell(row, col);
        if (c != null) {
            c.setStopTimer(stopTimerValue);
        }
    }

    protected void generatePassengers() {

        int nbNewPassengers;

        nbNewPassengers = new Random().nextInt(30);
        generatePassengers(-1, -1, nbNewPassengers);
    }

    protected void generatePassengers(int startStationId, int targetStationId, int nbNewPassengers) {

        int nbPassengersGenerated = 0;

        if (nbNewPassengers == -1) {
            nbNewPassengers = new Random().nextInt(30);
        }

        while (nbPassengersGenerated < nbNewPassengers) {

            StationCell startingStation = null;
            if (startStationId == -1) {
                int startingRank;
                // Choose one station at random
                startingRank = new Random().nextInt(stationList.size());
                startingStation = stationList.get(startingRank);
            } else {
                for (StationCell c : stationList) {
                    if (c.stationId == startStationId) {
                        startingStation = (StationCell) c;
                    }
                }
                if (startingStation == null) {
                    System.out.println("Error World, cannot find station " + startStationId);
                }
            }

            // Set the target of the passenger
            int currentTarget;
            if (targetStationId == -1) {
                int targetRank = new Random().nextInt(stationList.size());
                currentTarget = stationList.get(targetRank).getId();
            } else {
                currentTarget = targetStationId;
            }

            Passenger newPassenger = new Passenger(currentTarget);
            // Add the passenger to the station
            startingStation.addPassenger(newPassenger);
            if (!activeCells.contains(startingStation)) {
                activeCells.add(startingStation);
                startingStation.setActive(true);
            }
            nbPassengersGenerated++;
        }

        World.map.computeWalkways();
    }

    /**
     * Remove all passengers from all stations and all trains.
     */
    protected void removePassengers() {
        Iterator iter = activeCells.iterator();
        while (iter.hasNext()) {
            Cell c = (Cell) iter.next();
            c.removePassengers();
            if (!c.hasTrain()) {
                iter.remove();
                c.setActive(false);
            }
        }
        updateListeners();
    }

    protected void boardPassengers() {
        for (Cell c : activeCells) {
            c.boardPassengers();
        }
    }

    protected void getPassengersOff() {
        for (Cell c : activeCells) {
            c.getPassengersOff();
        }
    }

    /**
     * Map the links between the stations.
     */
    protected void clearMap() {
        System.out.println("Reset world map");
        map = new WorldMap(this);
    }

    protected void save(FileWriter writer) {
        try {
            writer.write(nbRows + " " + nbCols + "\n");

            writer.write("isRunning " + (isRunning ? YES : NO) + "\n");

            int rowIndex = 0;
            for (Cell[] row : cells) {
                int colIndex = 0;
                for (Cell c : row) {

                    // Save rails
                    if (c instanceof SwitchCell) {
                        // Save a switch cell
                        String switchText = SWITCH + " " + rowIndex + " " + colIndex + " " + ((SwitchCell) c).getLinks() + "\n";
                        writer.write(switchText);
                    } else {
                        String cellLinks = c.getLinks();
                        if (!cellLinks.isEmpty()) {
                            for (String singleLink : cellLinks.split(" ")) {
                                writer.write(RAIL_LINK + " " + rowIndex + " " + colIndex + " " + singleLink + "\n");
                            }
                        }
                    }

                    c.saveTrains(writer);

                    // Save stations
                    if (c instanceof StationCell) {
                        String text = STATION + " " + ((StationCell) c).getId() + " " + rowIndex + " " + colIndex + "\n";
                        writer.write(text);
                    }
                    c.savePassengers(writer);

                    // Save speed limits
                    if (c.speedLimit != Integer.MAX_VALUE) {
                        writer.write(SPEED_LIMIT + " " + rowIndex + " " + colIndex + " " + c.speedLimit + "\n");
                    }

                    // Save stop timers
                    if (c.stopTimerDuration > 0) {
                        writer.write(STOP_TIMER + " " + rowIndex + " " + colIndex + " " + c.stopTimerDuration + "\n");
                    }
                    colIndex++;
                }
                rowIndex++;
            }
            map.save(writer);

        } catch (IOException ex) {
            System.out.println("World: error occured when saving to file.");
        }

    }

    protected void load(Scanner scanner) {
        TrainElement.NB_TRAIN_ELEMENTS_CREATED = 0;
        String text = "";

        // Grid dimensions
        try {
            if (scanner.hasNextLine()) {
                text = scanner.nextLine();
                String split[] = text.split(" ");
                nbRows = Integer.valueOf(split[0]);
                nbCols = Integer.valueOf(split[1]);
                initializeGrid();
                updateListeners();
                StationCell.resetNbStationsCreated();
            }
        } catch (NumberFormatException e) {
            System.out.println("Error during file loading, text is " + text);
        }

        // Is the game running ?
        if (scanner.hasNextLine()) {
            text = scanner.nextLine();
            String split[] = text.split(" ");
            isRunning = split[1].equals(YES);
            if (isRunning) {
                startTimer();
            }
        }

        text = scanner.nextLine();
        while (scanner.hasNextLine() && !text.equals("map")) {
            String split[] = text.split(" ");
            int row, col;
            int id, wagonId;
            Cell c;
            double x, y, headingDegrees, linearSpeed, currentSpeedLimit;
            boolean isEngineActive, isBraking;
            int rank, targetStationId;
            Passenger newPassenger;
            CardinalPoint direction;

            switch (split[0]) {
            case STATION:
                id = Integer.valueOf(split[1]);
                row = Integer.valueOf(split[2]);
                col = Integer.valueOf(split[3]);
                toggleStation(row, col, id);
                break;
            case RAIL_LINK:
                row = Integer.valueOf(split[1]);
                col = Integer.valueOf(split[2]);
                c = getCellOrCreateIfNull(row, col);
                direction = CardinalPoint.valueOf(split[3]);
                c.addLink(direction);
                break;
            case SWITCH:
                row = Integer.valueOf(split[1]);
                col = Integer.valueOf(split[2]);
                // SWITCH 1 1 NORTH EAST WEST EAST
                c = getCellOrCreateIfNull(row, col);
                SwitchCell sc = new SwitchCell(c);
                setCell(row, col, sc);
                try {
                    sc.addConnections(
                            CardinalPoint.valueOf(split[3]),
                            CardinalPoint.valueOf(split[4]),
                            CardinalPoint.valueOf(split[5]));
                } catch (IllegalArgumentException e) {
                    System.out.println("Cannot load incomplete switch (row " + row + ", col " + col + ");");
                }

                break;
            case SPEED_LIMIT:
                row = Integer.valueOf(split[1]);
                col = Integer.valueOf(split[2]);
                c = getCell(row, col);
                double limit = Double.valueOf(split[3]);
                c.setSpeedIndicator(limit);
                break;
            case STOP_TIMER:
                row = Integer.valueOf(split[1]);
                col = Integer.valueOf(split[2]);
                c = getCell(row, col);
                double stopDuration = Double.valueOf(split[3]);
                c.setStopTimer(stopDuration);
                break;
            case LOCOMOTIVE:
                id = Integer.valueOf(split[1]);
                x = Double.valueOf(split[2]);
                y = Double.valueOf(split[3]);
                headingDegrees = Double.valueOf(split[4]);
                linearSpeed = Double.valueOf(split[5]);
                currentSpeedLimit = Double.valueOf(split[6]);
                isEngineActive = split[7].equals("engine_active");
                isBraking = split[8].equals("is_braking");
                addLoco(id, x, y, headingDegrees, linearSpeed, isEngineActive, isBraking, currentSpeedLimit);
                break;
            case WAGON:
                id = Integer.valueOf(split[1]);
                x = Double.valueOf(split[2]);
                y = Double.valueOf(split[3]);
                headingDegrees = Double.valueOf(split[4]);
                linearSpeed = Double.valueOf(split[5]);
                addWagon(id, x, y, headingDegrees, linearSpeed);
                break;
            case ONBOARD:
                // Onboard wagonId Passenger passId targetStation xPass yPass lastStep (...) firstStep
                wagonId = Integer.valueOf(split[1]);
                Wagon w = getWagon(wagonId);
                if (w != null) {
                    id = Integer.valueOf(split[3]);
                    targetStationId = Integer.valueOf(split[4]);
                    x = Double.valueOf(split[5]);
                    y = Double.valueOf(split[6]);
                    newPassenger = new Passenger(id, targetStationId, x, y);
                    for (rank = 7; rank < split.length; rank++) {
                        int newItineraryStep = Integer.valueOf(split[rank]);
                        newPassenger.addPathStep(newItineraryStep);
                    }
                    w.receivePassenger(newPassenger, true);
                }
                break;
            case PASSENGER:
                // Passenger id targetId x y pathsteps
                id = Integer.valueOf(split[1]);
                targetStationId = Integer.valueOf(split[2]);
                x = Double.valueOf(split[3]);
                y = Double.valueOf(split[4]);
                newPassenger = new Passenger(id, targetStationId, x, y);
                for (rank = 6; rank < split.length; rank++) {
                    int newItineraryStep = Integer.valueOf(split[rank]);
                    newPassenger.addPathStep(newItineraryStep);
                }
                col = getCol(x);
                row = getRow(y);
                Cell receivingCell = getCellOrCreateIfNull(row, col);
                if (receivingCell != null && receivingCell instanceof StationCell) {
                    ((StationCell) receivingCell).addPassenger(newPassenger);
                    if (!activeCells.contains(receivingCell)) {
                        activeCells.add(receivingCell);
                        receivingCell.setActive(true);
                    }
                }
                break;
            default:
                System.out.println("Error in file parsing. Text is " + text);
            }
            text = scanner.nextLine();
        }

        map.load(scanner);
        updateListeners();
    }

    protected StationCell getStation(int stationId) {
        for (StationCell station : stationList) {
            if (station.getId() == stationId) {
                return station;
            }
        }
        return null;
    }

    /**
     * Add a passenger to a specific wagon.
     *
     * @param newP the Passenger
     * @param id the (int)id of the chosen wagon
     */
    private void addPassenger(Passenger newP, int id) {
        for (Cell c : getAllCells()) {
            Wagon w = c.getWagon(id);
            if (w != null) {
                w.receivePassenger(newP);
            }
        }
    }

    /**
     * Add this many passengers to the specified station.
     *
     * @param nbPassengers
     * @param startStationId
     */
    void createPassengers(int nbPassengers, int startStationId, int endStationId) {
        StationCell s = getStation(startStationId);
        if (s != null) {
            for (int rank = 0; rank < nbPassengers; rank++) {
                Passenger newPassenger = new Passenger(endStationId);
                s.addPassenger(newPassenger);
            }
            if (!activeCells.contains(s)) {
                activeCells.add(s);
            }
        }
    }

    /**
     * Find and return the wagon with the given id.
     *
     * @param wagonId
     * @return the wagon with that id, or null if not found.
     */
    private Wagon getWagon(int wagonId) {
        Wagon w = null;
        for (Cell c : getAllCells()) {
            w = c.getWagon(wagonId);
            if (w != null) {
                return w;
            }
        }
        return null;
    }

    protected void setLongDistanceTracks(int rowParam, int colParam) {
        if (isSettingLongDistanceTracks) {
            // We already have selected a starting point for this track segment.

            int dRow = rowParam - longTrackStartRow;
            int dCol = colParam - longTrackStartCol;
            if (dRow == 0 || dCol == 0 || dRow == dCol || dRow == -dCol) {

                // rowStep and rowCol are chosen in {-1,0,1}
                int rowStep = getSign(dRow);
                int colStep = getSign(dCol);

                int currentRow = longTrackStartRow;
                int currentCol = longTrackStartCol;
                while (!(currentRow == rowParam && currentCol == colParam)) {
                    // Place one segment
                    setNewTrack(currentRow, currentCol, currentRow + rowStep, currentCol + colStep);
                    setNewTrack(currentRow + rowStep, currentCol + colStep, currentRow, currentCol);
                    currentRow += rowStep;
                    currentCol += colStep;
                }
            }
            isSettingLongDistanceTracks = false;
        } else {
            // We are choosing the origin of this track segment.
            longTrackStartRow = rowParam;
            longTrackStartCol = colParam;
            isSettingLongDistanceTracks = true;
        }
    }

    public boolean isSettingLongDistanceTracks() {
        return isSettingLongDistanceTracks;
    }

    public void cancelLongDistanceTracks() {
        isSettingLongDistanceTracks = false;
    }

    public int getLongDistanceTrackRow() {
        return longTrackStartRow;
    }

    public int getLongDistanceTrackCol() {
        return longTrackStartCol;
    }

    /**
     * Sign of an integer.
     *
     * @param val
     * @return +1 if the integer is greater than 0, -1 if it is less than 0,
     * 0 if it is equal to 0.
     */
    private int getSign(int val) {
        return val > 0 ? 1 : (val < 0 ? -1 : 0);
    }

    protected void setSwitchPoint(double currentRow, double currentCol) {
        Cell c = getCellOrCreateIfNull((int) currentRow, (int) currentCol);

        if (!c.hasTrain() && !(c instanceof StationCell)) {
            // Click coordinates in the cell's reference
            double localX = currentCol - floor(currentCol);
            double localY = currentRow - floor(currentRow);

            SwitchCell s;

            if (c instanceof SwitchCell) {
                // Add or replace a connecting CardinalPoint to the SwitchCell.
                activeCells.remove(c);
                s = (SwitchCell) c;
            } else {
                // Replace the Cell with a new SwitchCell and add the first CardinalPoint.
                removeCell(c);
                activeCells.remove(c);
                s = new SwitchCell(c);
                s.removeTracksAndLinks();
                addNewCell(s, (int) currentRow, (int) currentCol);
            }
            s.setSwitchPoint(localX, localY);
        }
    }

    protected void toggleSwitch(int row, int col) {
        Cell c = getCell(row, col);
        if (c instanceof SwitchCell) {
            ((SwitchCell) c).toggle();
        }
    }

    /**
     * Get the altitude at a specific location.
     * Use the cell already in cells list if it exists, otherwise compute the
     * altitude with the noise generator.
     *
     * @param col
     * @param row
     * @return the altitude at that location
     */
    protected double getAltitude(int col, int row) {

        double xCell = col * Cell.cellSize;
        double yCell = (getNbRows() - row - 1) * Cell.cellSize;

        return noiseGenerator.getNoise(xCell, yCell);
    }

    private void removeCell(Cell oldCell) {
        for (int row = 0; row < nbRows; row++) {
            for (int col = 0; col < nbCols; col++) {
                if (cells[row][col].equals(oldCell)) {
                    cells[row][col] = null;
                }
            }
        }
    }

    public ArrayList<Cell> getAvailableNeighbors(int row, int col) {

        ArrayList<Cell> neighbors = new ArrayList<>();

        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow != 0 || dCol != 0) {
                    Cell candidateNeighbor = getCell(row + dRow, col + dCol);
                    if (candidateNeighbor != null // Cell actually exists
                            && candidateNeighbor.altitude >= -0.05 // not sea
                            && candidateNeighbor.altitude <= 0.2 // not mountains
                            ) {
                        // The cell is buildable.
                        neighbors.add(candidateNeighbor);
                    }
                }
            }
        }
        return neighbors;
    }
}
