package minimetro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.ArrayList;

/**
 * This is a single 1x1 element of the grid.
 * It may contain one piece of track, one or several carriages.
 *
 * @author arthu
 */
public class Cell {

    private static boolean DISPLAY_ACTIVE_BORDERS = false;
    private static int DEFAULT_NB_RAILS = 10;

    protected static double cellSize = 100;

    protected Color color;

    protected ArrayList<TrainElement> trainElements;
    protected ArrayList<TransferringTrain> trainsLeavingCell;
    double maxHeadingDiff = 10;

    private ArrayList<CardinalPoint> links;
    private ArrayList<RailSegment> rails;
    private int nbRails;

    protected Point2D.Double absolutePosition; // This point is the center of the cell.

    protected double speedLimit; // Integer.MAX_VALUE if not set, -1 for end of limit, >0 for actual limit.
    // StopTimer: -1: no stopping required; >0: brake and stop for that many seconds.
    protected double stopTimerDuration;

    // The TrainElements that are currently stopped or have already stopped in
    // this cell and started moving again, and still are in this cell.
    private ArrayList<TrainElement> alreadyStoppedTrains;
    private Cell previous;

    private boolean isActive;

    private static int NB_CELLS_CREATED = 0;
    protected int id;

    public Cell() {
        color = Color.gray;
        trainElements = new ArrayList<>();
        trainsLeavingCell = new ArrayList<>();
        alreadyStoppedTrains = new ArrayList<>();
        rails = new ArrayList<>();
        nbRails = DEFAULT_NB_RAILS;
        absolutePosition = new Point2D.Double();
        links = new ArrayList<>();
        speedLimit = Integer.MAX_VALUE;
        isActive = false;
        id = NB_CELLS_CREATED;
        NB_CELLS_CREATED++;
    }

    public Cell(Cell previousCell) {
        this();
        if (previousCell != null) {
            this.absolutePosition = previousCell.absolutePosition;
            this.links = new ArrayList<>();
            for (CardinalPoint cp : previousCell.links) {
                this.links.add(cp);
            }
            this.rails = new ArrayList<>();
            for (RailSegment rail : previousCell.rails) {
                this.rails.add(new RailSegment(rail));
            }
            this.nbRails = previousCell.nbRails;
            this.speedLimit = previousCell.speedLimit;
            this.stopTimerDuration = previousCell.stopTimerDuration;
            this.isActive = previousCell.isActive;
        }
    }

    public Cell(Point2D.Double newAbsPos) {
        this();
        this.absolutePosition = newAbsPos;
    }

    /**
     * Paint the cell with its background and foreground.
     */
    protected void paintBackground(Graphics g, double x0, double y0, double zoom) {

        // On-screen coordinates of the center of the cell:
        final double xApp = absolutePosition.x * zoom + x0;
        final double yApp = g.getClipBounds().height - (absolutePosition.y * zoom + y0);
        final double appSize = zoom * cellSize;

        final double activeBorderSize = 0.05;
        if (DISPLAY_ACTIVE_BORDERS) {
            // Draw background
            if (isActive) {
                g.setColor(Color.cyan);
            } else {
                g.setColor(this.color);
            }
            // Border
            g.fillRect((int) (xApp - appSize * 0.5), (int) (yApp - appSize * 0.5), (int) (appSize), (int) (appSize));
            // Center
            g.setColor(this.color);
            g.fillRect((int) (xApp - appSize * (0.5 - activeBorderSize)), (int) (yApp - appSize * (0.5 - activeBorderSize)),
                    (int) (appSize * (1 - 2 * activeBorderSize)), (int) (appSize * (1 - 2 * activeBorderSize)));
        } else {
            g.setColor(this.color);
            g.fillRect((int) (xApp - appSize * 0.5), (int) (yApp - appSize * 0.5),
                    (int) appSize, (int) appSize);
        }
    }

    protected void paintForeground(Graphics g, double x0, double y0, double zoom) {
        // On-screen coordinates of the center of the cell:
        final double xApp = absolutePosition.x * zoom + x0;
        final double yApp = g.getClipBounds().height - (absolutePosition.y * zoom + y0);
        final double appSize = zoom * cellSize;

        if (rails != null && !rails.isEmpty()) {
            for (RailSegment railSegment : rails) {
                if (railSegment != null) {
                    railSegment.paint(g, x0, y0, zoom);
                }
            }
        }
        paintSpeedLimitSign(g, xApp, yApp, appSize);

        g.setColor(Color.black);
        Font font = new Font("helvetica", Font.PLAIN, 20);
        g.setFont(font);

        if (stopTimerDuration > 0) {
            String text = "Stop for " + stopTimerDuration + " seconds";
            g.drawString(text, (int) (xApp - appSize / 2 + 5), (int) (yApp - appSize / 2 + 15));
        }
    }

    /**
     * Paint the trainElements alone.
     * (to be used after all cells were drawn).
     */
    protected void paintTrains(Graphics g, double x0, double y0, double zoom) {
        if (hasTrain()) {
            for (TrainElement te : trainElements) {
                te.paint(g, x0, y0, zoom);
            }
        }
    }

    // A cell has tracks if it is linked to at least one neighbor.
    protected boolean hasRails() {
        return !rails.isEmpty();
    }

    protected void setLoco() {
        if (hasRails()) {
            Point2D.Double locoPosition = new Point2D.Double(absolutePosition.x,
                    absolutePosition.y);
            this.trainElements.add(0, new Locomotive(locoPosition));
            this.trainElements.get(0).headingDegrees = this.getCenterHeading();
        }
        snapToRail();
    }

    protected void setWagon() {
        if (hasRails()) {
            Point2D.Double wagonPosition = new Point2D.Double(absolutePosition.x,
                    absolutePosition.y);
            this.trainElements.add(0, new Wagon(wagonPosition));
            this.trainElements.get(0).headingDegrees = this.getCenterHeading();
        }
        snapToRail();
    }

    /**
     * Add a new TrainElement coming from the next cell with a predetermined
     * heading.
     *
     * @param newTrain
     */
    protected void addTrainElement(TrainElement newTrain) {
        this.trainElements.add(newTrain);
        snapToRail();
    }

    protected boolean hasLoco() {
        if (trainElements != null) {
            for (TrainElement te : trainElements) {
                if (te instanceof Locomotive) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean hasWagon() {
        if (trainElements != null) {
            for (TrainElement te : trainElements) {
                if (te instanceof Wagon) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean hasTrain() {
        return hasLoco() || hasWagon();
    }

    protected Locomotive getLoco() {
        if (trainElements != null) {
            for (TrainElement te : trainElements) {
                if (te instanceof Locomotive) {
                    return (Locomotive) te;
                }
            }
        }
        return null;
    }

    /**
     * Remove and return the trainElement if it exists, null otherwise.
     *
     * @return the trainElement if it exists, null otherwise.
     */
    protected TrainElement removeTrain() {
        TrainElement result = null;
        if (trainElements != null) {
            for (TrainElement te : trainElements) {
                if (te instanceof Locomotive) {
                    result = te;
                }
            }
        }
        if (result != null) {
            int index = trainElements.lastIndexOf(result);
            trainElements.remove(index);
            return result;
        }
        return null;
    }

    public void resetForces() {
        for (TrainElement te : trainElements) {
            te.resetForces();
        }
    }

    /**
     * Compute new speed based on engine power
     *
     * @param dt
     */
    protected void computeMotorForces(double dt) {
        for (TrainElement te : trainElements) {
            te.computeMotorForce(dt);
        }
    }

    /**
     * Compute new position based on current speed.
     *
     * @param dt
     */
    protected void moveTrains(double dt) {
        for (TrainElement trainElement : trainElements) {

            trainElement.move(dt);
            // Integer.MAX_VALUE if not set, -1 for end of limit, >0 for actual limit.
            if (speedLimit != Integer.MAX_VALUE) {
                trainElement.setSpeedLimit(speedLimit);
            }

            trainElement.observeCurrentSpeedLimit();

            observeStop(trainElement);

            CardinalPoint leavingDirection = isTrainElementLeaving(trainElement);
            if (leavingDirection != CardinalPoint.CENTER) {
                // The element has travelled to the next cell.
                trainsLeavingCell.add(new TransferringTrain(trainElement, leavingDirection));
                alreadyStoppedTrains.remove(trainElement);
            }
        }

    }

    /**
     * Compute the 2d position of the train from its linear position and the
     * tracks details.
     *
     * @return the 2d position relative to the current cell.
     */
    Point2D.Double getTrainPosition(TrainElement te) {
        return te.absolutePosition;
    }

    /**
     * Get the heading of the center of the track.
     *
     * @return the heading of the center of the track
     */
    protected double getCenterHeading() {
        return rails.get(nbRails / 2).getHeadingInDegrees();
    }

    protected TrainElement getTrainElement() {
        return this.trainElements.get(0);
    }

    /**
     * Get a list of all TrainElements in this cell.
     *
     * @return the list of TrainElements in this cell.
     */
    protected ArrayList<TrainElement> getAllElements() {
        ArrayList<TrainElement> copyList = new ArrayList<>();
        for (TrainElement te : this.trainElements) {
            copyList.add(te);
        }
        return copyList;
    }

    void flushMovingTrains() {
        for (TransferringTrain te : trainsLeavingCell) {
            int index = trainElements.lastIndexOf(te.getTrainElement());
            if (index >= 0) {
                trainElements.remove(index);
            }
        }
        trainsLeavingCell.clear();
    }

    boolean containsTrainElement(TrainElement requestedElement) {
        for (TrainElement te : trainElements) {
            if (te.equals(requestedElement)) {
                return true;
            }
        }
        return false;
    }

    void computeNewSpeeds(double dt) {
        for (TrainElement te : trainElements) {
            te.computeNewSpeed(dt);
        }
    }

    void setAbsoluteCoordinates(Point2D.Double cellPosition) {
        absolutePosition = cellPosition;
    }

    /**
     * Check whether a train is leaving a cell
     *
     * @param trainElement
     * @return the direction in which it is leaving.
     */
    private CardinalPoint isTrainElementLeaving(TrainElement trainElement) {

        if (trainElement.getX() < this.absolutePosition.x - cellSize / 2) {
            if (trainElement.getY() < this.absolutePosition.y - cellSize / 2) {
                return CardinalPoint.SOUTHWEST;
            } else if (trainElement.getY() > this.absolutePosition.y + cellSize / 2) {
                return CardinalPoint.NORTHWEST;
            } else {
                return CardinalPoint.WEST;
            }
        } else if (trainElement.getX() > this.absolutePosition.x + cellSize / 2) {
            if (trainElement.getY() < this.absolutePosition.y - cellSize / 2) {
                return CardinalPoint.SOUTHEAST;
            } else if (trainElement.getY() > this.absolutePosition.y + cellSize / 2) {
                return CardinalPoint.NORTHEAST;
            } else {
                return CardinalPoint.EAST;
            }
        } else {
            if (trainElement.getY() < this.absolutePosition.y - cellSize / 2) {
                return CardinalPoint.SOUTH;
            } else if (trainElement.getY() > this.absolutePosition.y + cellSize / 2) {
                return CardinalPoint.NORTH;
            } else {
                return CardinalPoint.CENTER;
            }
        }
    }

    /**
     * Reposition all TrainElements to the closest rail available.
     */
    void snapToRail() {
        for (TrainElement te : trainElements) {
            RailSegment closestRail = findClosestRailSegment(te);
            if (closestRail != null) {
                // snap the TrainElement to the rail
                closestRail.snapTrain(te);
            }
        }
    }

    /**
     * Find the rail that is closest to the TrainElement
     */
    protected RailSegment findClosestRailSegment(TrainElement te) {
        double minDistance = Double.MAX_VALUE;
        RailSegment closestSegment = null;
        for (RailSegment r : rails) {
            double distance = r.getDistance(te);
            if (distance < minDistance) {
                minDistance = distance;
                closestSegment = r;
            }
        }
        return closestSegment;
    }

    protected String getLinks() {
        String result = "";
        for (CardinalPoint link : links) {
            result += link + " ";
        }
        return result;
    }

    /**
     * Add a link from this cell to the specified direction.
     * A cell may have 0, 1 or 2 links (TODO: a switch shall behave differently)
     *
     * @param newLinkDirection
     */
    void addLink(CardinalPoint newLinkDirection) {
        if (newLinkDirection != null) {
            // The new direction must be placed in first position in the list (as the most recent one).
            if (links.contains(newLinkDirection)) {
                links.remove(newLinkDirection);
            }
            links.add(0, newLinkDirection);
        }
        // If the cell is already linked to two other cells, remove the oldest connection.
        if (links.size() > 2) {
            links.remove(2);
        }

        updateTracks();
    }

    /**
     * Test for 90° turns between horizontal and vertical.
     *
     * @return true when the selected exit is connected in an axis-aligned 90°
     * turn with the entrance.
     */
    private boolean isTrackTurningAxisAligned() {
        return this.isTrackTurningAxisAligned(0, 1);
    }

    private boolean isTrackTurningAxisAligned(int index0, int index1) {
        try {
            int difference = links.get(index0).difference(links.get(index1));
            boolean result = difference == 2 || difference == 6;
            return result;
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Test for 45° turns. Example: a track going from North to SouthWest
     *
     * @return
     */
    private boolean isTrackTurning45() {
        return this.isTrackTurning45(0, 1);
    }

    private boolean isTrackTurning45(int index0, int index1) {
        try {
            int difference = links.get(index0).difference(links.get(index1));
            boolean result = difference == 3 || difference == 5;
            return result;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Test for a track that does not turn within this cell.
     *
     * @return
     */
    private boolean isTrackStraight() {
        return this.isTrackStraight(0, 1);
    }

    private boolean isTrackStraight(int index0, int index1) {
        try {
            int difference = links.get(index0).difference(links.get(index1));
            boolean result = difference == 4;
            return result;

        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Test for a straight diagonal track.
     *
     * @return
     */
    private boolean isTrackDiagonal(int index0, int index1) {

        // Each link must be NE, SE, SW or NW.
        boolean result = (links.get(index0)).getIntValue() % 2 == 1
                && (links.get(index1)).getIntValue() % 2 == 1;

        return result;
    }

    /**
     * Set the rail segments in accordance with the cardinal points specified by
     * the link list.
     *
     */
    private void updateTracks() {

        rails.clear();

        if (isTrackTurningAxisAligned() || isTrackTurning45()) {
            updateTurningTracks();
        } else if (isTrackStraight()) {
            updateStraightTracks();
        }
    }

    /**
     * Create the rails when the tracks go N-S, E-W, NW-SE or SW-NE, without
     * turns.
     */
    private void updateStraightTracks() {
        updateStraightTracks(0, 1, rails);
    }

    private void updateStraightTracks(int firstIndex, int secondIndex, ArrayList<RailSegment> railsParam) {
        double xCell = absolutePosition.x, yCell = absolutePosition.y;
        double xStart = xCell, xEnd = xCell, yStart = yCell, yEnd = yCell;

        if (links.size() >= firstIndex) {
            CardinalPoint firstLink = links.get(firstIndex);
            switch (firstLink) {
            case WEST:
                xStart = xCell - cellSize / 2;
                yStart = yCell;
                break;
            case NORTHWEST:
                xStart = xCell - cellSize / 2;
                yStart = yCell + cellSize / 2;
                break;
            case NORTH:
                xStart = xCell;
                yStart = yCell + cellSize / 2;
                break;
            case NORTHEAST:
                xStart = xCell + cellSize / 2;
                yStart = yCell + cellSize / 2;
                break;
            case EAST:
                xStart = xCell + cellSize / 2;
                yStart = yCell;
                break;
            case SOUTHEAST:
                xStart = xCell + cellSize / 2;
                yStart = yCell - cellSize / 2;
                break;
            case SOUTH:
                xStart = xCell;
                yStart = yCell - cellSize / 2;
                break;
            case SOUTHWEST:
                xStart = xCell - cellSize / 2;
                yStart = yCell - cellSize / 2;
                break;
            default:
                xStart = xCell;
                yStart = yCell;
            }
        }
        if (links.size() >= secondIndex) {
            CardinalPoint secondLink = links.get(secondIndex);
            switch (secondLink) {
            case WEST:
                xEnd = xCell - cellSize / 2;
                yEnd = yCell;
                break;
            case NORTHWEST:
                xEnd = xCell - cellSize / 2;
                yEnd = yCell + cellSize / 2;
                break;
            case NORTH:
                xEnd = xCell;
                yEnd = yCell + cellSize / 2;
                break;
            case NORTHEAST:
                xEnd = xCell + cellSize / 2;
                yEnd = yCell + cellSize / 2;
                break;
            case EAST:
                xEnd = xCell + cellSize / 2;
                yEnd = yCell;
                break;
            case SOUTHEAST:
                xEnd = xCell + cellSize / 2;
                yEnd = yCell - cellSize / 2;
                break;
            case SOUTH:
                xEnd = xCell;
                yEnd = yCell - cellSize / 2;
                break;
            case SOUTHWEST:
                xEnd = xCell - cellSize / 2;
                yEnd = yCell - cellSize / 2;
                break;
            default:
                xEnd = xCell;
                yEnd = yCell;
            }
        }

        int actualNbRails = nbRails;
        if (isTrackDiagonal(firstIndex, secondIndex)) {
            actualNbRails = (int) (nbRails * 1.41);
        }
        for (int i = 0; i < actualNbRails; i++) {
            double x0 = xStart + i * (xEnd - xStart) / actualNbRails;
            double y0 = yStart + i * (yEnd - yStart) / actualNbRails;
            double x1 = xStart + (i + 1) * (xEnd - xStart) / actualNbRails;
            double y1 = yStart + (i + 1) * (yEnd - yStart) / actualNbRails;
            railsParam.add(new RailSegment(x0, y0, x1, y1));
        }
    }

    /**
     * Create the rails when the tracks go from N-S to E-W, with a turn,
     * or when they go from axis-aligned to diagonal.
     */
    private void updateTurningTracks() {
        this.updateTurningTracks(0, 1, this.rails);
    }

    private void updateTurningTracks(int index0, int index1, ArrayList<RailSegment> railsParam) {

        double xCenter = 0, yCenter = 0;
        double radius;
        double angleStart = 0, angleEnd = 0;

        // Add the curved part for both 45 and 90 degrees turns.
        if (isTrackTurning45(index0, index1)) {
            radius = cellSize * (1 + 1.414) / 2;

            // Horizontal EAST
            if (isLinked(CardinalPoint.NORTHWEST, index0, index1) && isLinked(CardinalPoint.EAST, index0, index1)) {
                xCenter = absolutePosition.x + cellSize / 2;
                yCenter = absolutePosition.y + cellSize * (2.414 / 2);
                angleStart = 5 * PI / 4;
                angleEnd = 3 * PI / 2;
            } else if (isLinked(CardinalPoint.SOUTHWEST, index0, index1) && isLinked(CardinalPoint.EAST, index0, index1)) {
                xCenter = absolutePosition.x + cellSize / 2;
                yCenter = absolutePosition.y - cellSize * (2.414 / 2);
                angleStart = 3 * PI / 4;
                angleEnd = PI / 2;
            } // Horizontal WEST
            else if (isLinked(CardinalPoint.NORTHEAST, index0, index1) && isLinked(CardinalPoint.WEST, index0, index1)) {
                xCenter = absolutePosition.x - cellSize / 2;
                yCenter = absolutePosition.y + cellSize * (2.414 / 2);
                angleStart = 3 * PI / 2;
                angleEnd = 7 * PI / 4;
            } else if (isLinked(CardinalPoint.SOUTHEAST, index0, index1) && isLinked(CardinalPoint.WEST, index0, index1)) {
                xCenter = absolutePosition.x - cellSize / 2;
                yCenter = absolutePosition.y - cellSize * (2.414 / 2);
                angleStart = PI / 2;
                angleEnd = PI / 4;
            } // Vertical NORTH
            else if (isLinked(CardinalPoint.NORTH, index0, index1) && isLinked(CardinalPoint.SOUTHWEST, index0, index1)) {
                xCenter = absolutePosition.x - cellSize * (2.414 / 2);
                yCenter = absolutePosition.y + cellSize / 2;
                angleStart = 0;
                angleEnd = -PI / 4;
            } else if (isLinked(CardinalPoint.NORTH, index0, index1) && isLinked(CardinalPoint.SOUTHEAST, index0, index1)) {
                xCenter = absolutePosition.x + cellSize * (2.414 / 2);
                yCenter = absolutePosition.y + cellSize / 2;
                angleStart = PI;
                angleEnd = 5 * PI / 4;
            } // Vertical SOUTH
            else if (isLinked(CardinalPoint.SOUTH, index0, index1) && isLinked(CardinalPoint.NORTHWEST, index0, index1)) {
                xCenter = absolutePosition.x - cellSize * (2.414 / 2);
                yCenter = absolutePosition.y - cellSize / 2;
                angleStart = 0;
                angleEnd = PI / 4;
            } else if (isLinked(CardinalPoint.SOUTH, index0, index1) && isLinked(CardinalPoint.NORTHEAST, index0, index1)) {
                xCenter = absolutePosition.x + cellSize * (2.414 / 2);
                yCenter = absolutePosition.y - cellSize / 2;
                angleStart = PI;
                angleEnd = 3 * PI / 4;
            }

        } else {
            // Axis-aligned right-angled turns
            radius = cellSize / 2;
            if (isLinked(CardinalPoint.SOUTH, index0, index1) && isLinked(CardinalPoint.EAST, index0, index1)) {
                xCenter = absolutePosition.x + cellSize / 2;
                yCenter = absolutePosition.y - cellSize / 2;
                angleStart = PI;
                angleEnd = PI / 2;
                radius = cellSize / 2;
            } else if (isLinked(CardinalPoint.EAST, index0, index1) && isLinked(CardinalPoint.NORTH, index0, index1)) {
                xCenter = absolutePosition.x + cellSize / 2;
                yCenter = absolutePosition.y + cellSize / 2;
                angleStart = 3 * PI / 2;
                angleEnd = PI;
                radius = cellSize / 2;
            } else if (isLinked(CardinalPoint.NORTH, index0, index1) && isLinked(CardinalPoint.WEST, index0, index1)) {
                xCenter = absolutePosition.x - cellSize / 2;
                yCenter = absolutePosition.y + cellSize / 2;
                angleStart = 4 * PI / 2;
                angleEnd = 3 * PI / 2;
                radius = cellSize / 2;
            } else if (isLinked(CardinalPoint.WEST, index0, index1) && isLinked(CardinalPoint.SOUTH, index0, index1)) {
                xCenter = absolutePosition.x - cellSize / 2;
                yCenter = absolutePosition.y - cellSize / 2;
                angleStart = PI / 2;
                angleEnd = 0;
                radius = cellSize / 2;
            }
        }

        for (int i = 0; i < nbRails; i++) {
            double x0 = xCenter + radius * cos(angleStart + i * (angleEnd - angleStart) / nbRails);
            double y0 = yCenter + radius * sin(angleStart + i * (angleEnd - angleStart) / nbRails);
            double x1 = xCenter + radius * cos(angleStart + (i + 1) * (angleEnd - angleStart) / nbRails);
            double y1 = yCenter + radius * sin(angleStart + (i + 1) * (angleEnd - angleStart) / nbRails);
            railsParam.add(new RailSegment(x0, y0, x1, y1));
        }

        // 45 degrees turns require an additional straight part
        if (isTrackTurning45(index0, index1)) {
            double unitLength = (2 - 1.414) / 4; // The length of the straight part
            int nbAdditionalSegments = 4;
            double dx = unitLength / nbAdditionalSegments;

            for (int i = 0; i < nbAdditionalSegments; i++) {
                if (isLinked(CardinalPoint.NORTHEAST, index0, index1)) {
                    railsParam.add(new RailSegment(absolutePosition.x + cellSize * (0.5 - i * dx), absolutePosition.y + cellSize * (0.5 - i * dx),
                            absolutePosition.x + cellSize * (0.5 - (i + 1) * dx), absolutePosition.y + cellSize * (0.5 - (i + 1) * dx)));
                } else if (isLinked(CardinalPoint.SOUTHEAST, index0, index1)) {
                    railsParam.add(new RailSegment(absolutePosition.x + cellSize * (0.5 - i * dx), absolutePosition.y - cellSize * (0.5 - i * dx),
                            absolutePosition.x + cellSize * (0.5 - (i + 1) * dx), absolutePosition.y - cellSize * (0.5 - (i + 1) * dx)));
                } else if (isLinked(CardinalPoint.SOUTHWEST, index0, index1)) {
                    railsParam.add(new RailSegment(absolutePosition.x - cellSize * (0.5 - i * dx), absolutePosition.y - cellSize * (0.5 - i * dx),
                            absolutePosition.x - cellSize * (0.5 - (i + 1) * dx), absolutePosition.y - cellSize * (0.5 - (i + 1) * dx)));
                } else if (isLinked(CardinalPoint.NORTHWEST, index0, index1)) {
                    railsParam.add(new RailSegment(absolutePosition.x - cellSize * (0.5 - i * dx), absolutePosition.y + cellSize * (0.5 - i * dx),
                            absolutePosition.x - cellSize * (0.5 - (i + 1) * dx), absolutePosition.y + cellSize * (0.5 - (i + 1) * dx)));
                }
            }
        }
    }

    /**
     * Tell if a cell is completely empty.
     *
     * @return true when the cell contains no rails, loco, wagon, passengers,
     * and is not a station; false otherwise.
     */
    protected boolean isEmpty() {
        return trainElements.isEmpty() && rails.isEmpty();
    }

    /**
     * Return true when the cell has a link in the specified direction.
     *
     * @param cardinalPoint
     * @param index0 the first exit we're testing (can be 0 to 1 for regular
     * tracks, or 0 to 3 for switches)
     * @param index1 the second exit we're testing (can be 0 to 1 for regular
     * tracks, or 0 to 3 for switches)
     * @return
     */
    protected boolean isLinked(CardinalPoint cardinalPoint, int index0, int index1) {
        try {
            if (links.isEmpty()) {
                return false;
            }
            return links.size() > index0 && links.get(index0) == cardinalPoint
                    || links.size() > index1 && links.get(index1) == cardinalPoint;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    protected boolean isLinked(CardinalPoint cardinalPoint) {
        return isLinked(cardinalPoint, 0, 1);
    }

    protected void removeTracksAndLinks() {
        if (!hasTrain()) {
            rails.clear();
            links.clear();
        }
    }

    protected void removeTrains() {
        trainElements.clear();
        trainsLeavingCell.clear();
    }

    protected void startLocos() {
        for (TrainElement te : trainElements) {
            te.start();
        }
    }

    protected void stopLocos() {
        for (TrainElement te : trainElements) {
            te.stop();
        }
    }

    /**
     * Set the speed limit for that cell.
     * Setting the same limit as the cell already has shall deactivate the
     * limit.
     *
     * @param speedIndicatorValue
     */
    protected void setSpeedIndicator(double speedIndicatorValue) {
        if (speedLimit == speedIndicatorValue) {
            speedLimit = Integer.MAX_VALUE;
        } else {
            speedLimit = speedIndicatorValue;
        }
    }

    /**
     * Set how long locos have to remain stopped at this cell.
     * Setting the same value as the cell already has shall deactivate the stop
     * timer.
     *
     * @param newStopTimerDuration
     */
    protected void setStopTimer(double newStopTimerDuration) {
        if (stopTimerDuration == newStopTimerDuration) {
            stopTimerDuration = -1;
        } else {
            stopTimerDuration = newStopTimerDuration;
        }
    }

    /**
     * Paint a speed limit roadsign, with the value in a circle.
     *
     * @param xApp
     * @param yApp
     * @param appSize
     */
    private void paintSpeedLimitSign(Graphics g, double xApp, double yApp, double appSize) {

        String text = "";
        boolean mustDisplaySign = false;

        if (speedLimit == -1) {
            text = "End";
            mustDisplaySign = true;
        } else if (speedLimit == Integer.MAX_VALUE) {
            // Not set
        } else {
            // Actual positive value
            if (speedLimit >= 10) {
                text = "" + (int) speedLimit;
            } else {
                text = "" + speedLimit;
            }
            mustDisplaySign = true;
        }

        if (mustDisplaySign) {
            // Find a spot in the cell far enough from the railroad
            // Default spot is above the center of the cell, halfway from the North border.
            double xSign = xApp;
            double ySign = yApp;

            if (isLinked(CardinalPoint.NORTH, 0, 1) || isLinked(CardinalPoint.NORTH, 2, 3)) {
                // Second spot, halfway to the South border
                if (isLinked(CardinalPoint.SOUTH, 0, 1) || isLinked(CardinalPoint.SOUTH, 2, 3)) {
                    // Cell is linked North and South, the sign shall be placed in the East.
                    xSign += appSize / 4;
                } else {
                    ySign += appSize / 4;
                }
            } else {
                ySign -= appSize / 4;
            }

            double diskRadius = appSize / 8;
            g.setColor(Color.red);
            g.fillOval((int) (xSign - diskRadius), (int) (ySign - diskRadius), (int) (2 * diskRadius), (int) (2 * diskRadius));
            g.setColor(Color.black);
            g.drawOval((int) (xSign - diskRadius), (int) (ySign - diskRadius), (int) (2 * diskRadius), (int) (2 * diskRadius));
            g.setColor(Color.white);
            diskRadius = 0.8 * diskRadius;
            g.fillOval((int) (xSign - diskRadius), (int) (ySign - diskRadius), (int) (2 * diskRadius), (int) (2 * diskRadius));
            g.setColor(Color.black);
            g.setFont(new Font("helvetica", Font.PLAIN, (int) (appSize / 15)));
            int textWidth = g.getFontMetrics().stringWidth(text);
            int textHeight = g.getFontMetrics().getHeight();
            g.drawString(text, (int) xSign - textWidth / 2, (int) ySign + textHeight / 2);
        }
    }

    private void observeStop(TrainElement trainElement) {

        if (stopTimerDuration > 0) {

            if (!alreadyStoppedTrains.contains(trainElement)) {
                // Set the train to stop for the requested duration.
                alreadyStoppedTrains.add(trainElement);
                trainElement.setTimedStop(this.stopTimerDuration);
            }
        }
        // otherwise this cell does not stop trains.
    }

    protected Point2D.Double getAbsolutePosition() {
        return this.absolutePosition;
    }

    protected void getPassengersOff() {

    }

    protected void boardPassengers() {

    }

    /**
     * Get the direction of one of the two connections of that cell to a
     * neighbor;
     * but not the direction leading to where we're coming from.
     * Example: calling that method on a cell linked NORTH-SOUTH with
     * comingFrom=North, we must return south.
     *
     * @param comingFrom
     * @return
     */
    protected CardinalPoint getDirectionExcept(CardinalPoint comingFrom) {
        if (!this.hasRails()) {
            return null;
        }

        CardinalPoint forbiddenDirection = CardinalPoint.getOpposite(comingFrom);
        if (links.get(0).equals(forbiddenDirection)) {
            return links.get(1);
        } else {
            return links.get(0);
        }
    }

    /**
     * Only StationCells do that.
     */
    protected void movePassengers(double dt) {
    }

    /**
     * Only StationCells do that.
     */
    protected void addWalkwayDirection(StationCell cell, CardinalPoint cardinalPoint) {
    }

    protected void saveTrains(FileWriter writer) {
        for (TrainElement te : trainElements) {
            te.save(writer);
        }
    }

    protected void savePassengers(FileWriter writer) {
    }

    protected Wagon getWagon(int requestedId) {
        for (TrainElement te : trainElements) {
            if (te instanceof Wagon && te.id == requestedId) {
                Wagon w = (Wagon) te;
                return w;
            }
        }
        return null;
    }

    protected void removePassengers() {
        for (TrainElement te : trainElements) {
            if (te instanceof Wagon) {
                ((Wagon) te).dropAllPassengers();
            }
        }
    }

    protected int getNbPassengers() {
        return 0;
    }

    protected void setActive(boolean b) {
        isActive = b;
    }

    protected boolean hasPassengers() {
        return false;
    }

    protected ArrayList<TransferringTrain> getTrainsLeavingCell() {
        return this.trainsLeavingCell;
    }
}
