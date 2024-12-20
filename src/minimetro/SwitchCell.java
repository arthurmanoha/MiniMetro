package minimetro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 *
 * A SwitchCell is a Cell with two sets of railways.
 * It is made with two distinct Cells with tracks that have one point in common.
 *
 * @author arthu
 */
public class SwitchCell extends Cell {

    private Cell cellA, cellB;

    // Default: cellA is active; when toggled: cellB is active.
    private boolean isToggled;

    // Max distance to the rails. Above that value, trains are added to the other sub-cell.
    private static double MAX_DISTANCE = 0.1 * Cell.cellSize;

    private static int NB_SWITCHES_CREATED = 0;
    private int id;

    // First entry: shared neighbor
    // Second and third: independent neighbors
    private CardinalPoint connections[];

    public SwitchCell() {
        this(null);
        id = NB_SWITCHES_CREATED;
        NB_SWITCHES_CREATED++;
    }

    public SwitchCell(Cell c) {
        super(c);
        if (c == null) {
            cellA = new Cell(c);
        } else {
            cellA = c;
        }
        cellA.setActive(false);
        cellB = new Cell(cellA);
        cellB.setActive(false);
        this.row = c.row;
        this.col = c.col;
        cellA.row = c.row;
        cellA.col = c.col;
        cellB.row = c.row;
        cellB.col = c.col;
        connections = new CardinalPoint[3];
        connections[0] = null;
        connections[1] = null;
        connections[2] = null;
        isToggled = false;

        // Change indices such that the switch cell is #n and the two sub-cells are #n+1 and #n+2:
        int prevId = cellA.id;
        cellA.id = super.id;
        super.id = prevId;
    }

    @Override
    protected void paintForeground(Graphics g, double x0, double y0, double zoom) {
        if (isToggled) {
            // B is active and painted last
            cellA.paintForeground(g, x0, y0, zoom);
            cellB.paintForeground(g, x0, y0, zoom);
        } else {
            // A is active and painted last
            cellB.paintForeground(g, x0, y0, zoom);
            cellA.paintForeground(g, x0, y0, zoom);
        }
        final double xApp = absolutePosition.x * zoom + x0;
        final double yApp = g.getClipBounds().height - (absolutePosition.y * zoom + y0);
        final double appSize = zoom * cellSize;

        paintActiveCellIndicator(g, xApp, yApp, appSize);

        // Display the direction as text only if at least one is undefined.
        if (isSwitchIncomplete()) {
            String text = "";
            for (CardinalPoint cp : connections) {
                if (cp == null) {
                    text += "-";
                } else {
                    text += (cp + "").substring(0, 1) + "";
                }
            }
            g.setColor(Color.black);
            Font font = new Font("helvetica", Font.PLAIN, 20);
            g.setFont(font);
            g.drawString(text, (int) (xApp - appSize / 2 + 5), (int) (yApp - appSize / 2 + 20));
        }
    }

    /**
     * Test if the switch is complete.
     *
     * @return true when the switch is incomplete, i.e. not all its connections
     * are specified.
     */
    private boolean isSwitchIncomplete() {
        for (CardinalPoint cp : connections) {
            if (cp == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Paint the trainElements alone.
     * (to be used after all cells were drawn).
     */
    @Override
    protected void paintTrains(Graphics g, double x0, double y0, double zoom) {
        cellA.paintTrains(g, x0, y0, zoom);
        cellB.paintTrains(g, x0, y0, zoom);
    }

    /**
     * Add a new direction at the end.
     * The second direction becomes the shared first one, third becomes second.
     *
     * @param x
     * @param y
     */
    protected void setSwitchPoint(double x, double y) {
        CardinalPoint cp = computeDirection(x, y);
        addConnection(cp);
    }

    private void addConnection(CardinalPoint cp) {

        connections[0] = connections[1];
        connections[1] = connections[2];
        connections[2] = cp;

        if (connections[0] != null) {
            cellA.removeTracksAndLinks();
            cellB.removeTracksAndLinks();
            // First entry: shared neighbor
            // Second and third: independent neighbors
            cellA.addLink(connections[0]);
            cellA.addLink(connections[1]);
            cellB.addLink(connections[0]);
            cellB.addLink(connections[2]);
        }
    }

    protected void addConnections(CardinalPoint connection0, CardinalPoint connection1, CardinalPoint connection2) {
        addConnection(connection0);
        addConnection(connection1);
        addConnection(connection2);
    }

    @Override
    protected void removeTracksAndLinks() {
        cellA.removeTracksAndLinks();
        cellB.removeTracksAndLinks();
    }

    private CardinalPoint computeDirection(double x, double y) {
        if (x < 0.33) {
            // W
            if (y < 0.33) {
                // NW
                return CardinalPoint.NORTHWEST;
            } else if (y > 0.67) {
                // SW
                return CardinalPoint.SOUTHWEST;
            } else {
                // W
                return CardinalPoint.WEST;
            }
        } else if (x > 0.67) {
            // E
            if (y < 0.33) {
                // NE
                return CardinalPoint.NORTHEAST;
            } else if (y > 0.67) {
                // SE
                return CardinalPoint.SOUTHEAST;
            } else {
                // E
                return CardinalPoint.EAST;
            }
        } else {
            if (y < 0.33) {
                // N
                return CardinalPoint.NORTH;
            } else if (y > 0.67) {
                // S
                return CardinalPoint.SOUTH;
            } else {
                // C
                return CardinalPoint.CENTER;
            }
        }
    }

    @Override
    protected String getLinks() {
        return connections[0] + " " + connections[1] + " " + connections[2];
    }

    /**
     * Add a train element to this SwitchCell.Choose the sub-cell that will
     * receive the element:
     * - a train coming from the shared direction shall be added to the "active"
     * sub-cell;
     * - a train coming from either of the other two direction shall be added to
     * the sub-cell linked in that direction.
     *
     * @param newTrain
     */
    @Override
    protected void addTrainElement(TrainElement newTrain) {
        Cell chosenCell = chooseSubCell(newTrain);
        chosenCell.addTrainElement(newTrain);
    }

    private Cell chooseSubCell(TrainElement newTrain) {
        RailSegment railA = cellA.findClosestRailSegment(newTrain);
        RailSegment railB = cellB.findClosestRailSegment(newTrain);

        double distanceA;
        if (railA == null) {
            distanceA = Double.MAX_VALUE;
        } else {
            distanceA = railA.getDistance(newTrain);
        }
        double distanceB;
        if (railB == null) {
            distanceB = Double.MAX_VALUE;
        } else {
            distanceB = railB.getDistance(newTrain);
        }

        Cell chosenCell;
        if (distanceA > MAX_DISTANCE) {
            // Train is coming through connection specific to cellB
            chosenCell = cellB;
        } else if (distanceB > MAX_DISTANCE) {
            // Train is coming through connection specific to cellA
            chosenCell = cellA;
        } else {
            // Train is coming through the shared connection.
            if (isToggled) {
                chosenCell = cellB;
            } else {
                chosenCell = cellA;
            }
        }
        return chosenCell;
    }

    /**
     * Choose which sub-cell we should choose to snap the train elements.
     */
    @Override
    protected void snapToRail() {
        cellA.snapToRail();
        cellB.snapToRail();
    }

    @Override
    public void resetForces() {
        cellA.resetForces();
        cellB.resetForces();
    }

    @Override
    protected void computeMotorForces(double dt) {
        cellA.computeMotorForces(dt);
        cellB.computeMotorForces(dt);
    }

    @Override
    void computeNewSpeeds(double dt) {
        cellA.computeNewSpeeds(dt);
        cellB.computeNewSpeeds(dt);
    }

    @Override
    protected void moveTrains(double dt) {
        cellA.moveTrains(dt);
        cellB.moveTrains(dt);

        this.trainsLeavingCell.addAll(cellA.trainsLeavingCell);
        this.trainsLeavingCell.addAll(cellB.trainsLeavingCell);
    }

    @Override
    void flushMovingTrains() {
        super.flushMovingTrains();
        cellA.flushMovingTrains();
        cellB.flushMovingTrains();
    }

    @Override
    protected boolean hasTrain() {
        return cellA.hasTrain() || cellB.hasTrain();
    }

    @Override
    protected boolean hasRails() {
        return cellA.hasRails() || cellB.hasRails();
    }

    @Override
    protected boolean hasPassengers() {
        return cellA.hasPassengers() || cellB.hasPassengers();
    }

    @Override
    protected ArrayList<TrainElement> getAllElements() {
        ArrayList<TrainElement> result = new ArrayList<>();
        result.addAll(cellA.getAllElements());
        result.addAll(cellB.getAllElements());
        return result;
    }

    @Override
    protected ArrayList<TransferringTrain> getTrainsLeavingCell() {
        ArrayList<TransferringTrain> res = new ArrayList<>();
        res.addAll(cellA.getTrainsLeavingCell());
        res.addAll(cellB.getTrainsLeavingCell());
        return res;
    }

    @Override
    boolean containsTrainElement(TrainElement requestedElement) {
        return cellA.containsTrainElement(requestedElement) || cellB.containsTrainElement(requestedElement);
    }

    /**
     * Tell if a cell is completely empty.
     *
     * @return true when the cell contains no rails, loco, wagon, passengers,
     * and is not a station; false otherwise.
     */
    @Override
    protected boolean isEmpty() {
        return cellA.isEmpty() && cellB.isEmpty();
    }

    @Override
    protected void setActive(boolean b) {
        super.setActive(b);
        cellA.setActive(false);
        cellB.setActive(false);
    }

    @Override
    protected void startLocos() {
        cellA.startLocos();
        cellB.startLocos();
    }

    @Override
    protected void removeTrains() {
        cellA.removeTrains();
        cellB.removeTrains();
    }

    @Override
    protected void saveTrains(FileWriter writer) {
        cellA.saveTrains(writer);
        cellB.saveTrains(writer);
    }

    @Override
    protected void setSpeedIndicator(double speedIndicatorValue) {
        cellA.setSpeedIndicator(speedIndicatorValue);
        cellB.setSpeedIndicator(speedIndicatorValue);
    }

    @Override
    protected void setStopTimer(double newStopTimerDuration) {
        cellA.setStopTimer(newStopTimerDuration);
        cellB.setStopTimer(newStopTimerDuration);
    }

    private void paintActiveCellIndicator(Graphics g, double xApp, double yApp, double appSize) {
        double xStart, yStart, xEnd, yEnd;
        try {
            switch (connections[0]) {
            case WEST:
                xStart = xApp - appSize / 2;
                yStart = yApp;
                break;
            case NORTHWEST:
                xStart = xApp - appSize / 2;
                yStart = yApp - appSize / 2;
                break;
            case NORTH:
                xStart = xApp;
                yStart = yApp - appSize / 2;
                break;
            case NORTHEAST:
                xStart = xApp + appSize / 2;
                yStart = yApp - appSize / 2;
                break;
            case EAST:
                xStart = xApp + appSize / 2;
                yStart = yApp;
                break;
            case SOUTHEAST:
                xStart = xApp + appSize / 2;
                yStart = yApp + appSize / 2;
                break;
            case SOUTH:
                xStart = xApp;
                yStart = yApp + appSize / 2;
                break;
            case SOUTHWEST:
                xStart = xApp - appSize / 2;
                yStart = yApp + appSize / 2;
                break;
            default:
                xStart = 0;
                yStart = 0;
            }

            CardinalPoint activeExit;
            if (isToggled) {
                activeExit = connections[2];
            } else {
                activeExit = connections[1];
            }

            switch (activeExit) {
            case WEST:
                xEnd = xApp - appSize / 2;
                yEnd = yApp;
                break;
            case NORTHWEST:
                xEnd = xApp - appSize / 2;
                yEnd = yApp - appSize / 2;
                break;
            case NORTH:
                xEnd = xApp;
                yEnd = yApp - appSize / 2;
                break;
            case NORTHEAST:
                xEnd = xApp + appSize / 2;
                yEnd = yApp - appSize / 2;
                break;
            case EAST:
                xEnd = xApp + appSize / 2;
                yEnd = yApp;
                break;
            case SOUTHEAST:
                xEnd = xApp + appSize / 2;
                yEnd = yApp + appSize / 2;
                break;
            case SOUTH:
                xEnd = xApp;
                yEnd = yApp + appSize / 2;
                break;
            case SOUTHWEST:
                xEnd = xApp - appSize / 2;
                yEnd = yApp + appSize / 2;
                break;
            default:
                xEnd = 0;
                yEnd = 0;
            }
            g.setColor(Color.red);
            g.drawLine((int) xStart, (int) yStart, (int) xEnd, (int) yEnd);
        } catch (NullPointerException e) {

        }
    }

    /**
     * Change which sub-cell is active.
     */
    protected void toggle() {
        isToggled = !isToggled;
    }
}
