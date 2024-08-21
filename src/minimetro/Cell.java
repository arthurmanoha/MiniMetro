package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
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

    protected static double cellSize = 1;

    protected Color color;

    private ArrayList<TrainElement> trainElements;
    public ArrayList<TransferringTrain> trainsLeavingCell;
    double maxHeadingDiff = 10;

    private ArrayList<RailSegment> rails;
    private int nbRails;
    private double totalRailLength;
    private double singleRailLength;
    ArrayList<Color> colorList;

    private Point2D.Double absolutePosition; // This point is the center of the cell.

    private ArrayList<CardinalPoint> links;

    public Cell() {
        color = Color.gray;
        trainElements = new ArrayList<>();
        trainsLeavingCell = new ArrayList<>();
        rails = new ArrayList<>();
        totalRailLength = cellSize;
        nbRails = 20;
        singleRailLength = totalRailLength / nbRails;
        colorList = new ArrayList<>();
        colorList.add(Color.red);
        colorList.add(Color.green);
        colorList.add(Color.blue);
        colorList.add(Color.yellow);
        colorList.add(Color.gray);
        colorList.add(Color.orange);
        colorList.add(Color.MAGENTA);
        colorList.add(Color.CYAN);
        absolutePosition = new Point2D.Double();
        links = new ArrayList<>();
        links.add(CardinalPoint.CENTER);
        links.add(CardinalPoint.CENTER);
    }

    public Cell(Point2D.Double newAbsPos) {
        this();
        this.absolutePosition = newAbsPos;
    }

    public Cell(ArrayList<RailSegment> oldRails) {

        this();

        if (oldRails == null) {
            rails = new ArrayList<>();
            nbRails = 10;
        } else {
            rails = oldRails;
            nbRails = rails.size();
        }

    }

    /**
     * Paint the cell with its background and foreground.
     */
    protected void paint(Graphics g, double x0, double y0, double zoom) {

        // On-screen coordinates of the center of the cell:
        final double xApp = absolutePosition.x * zoom + x0;
        final double yApp = g.getClipBounds().height - (absolutePosition.y * zoom + y0);
        final double appSize = zoom * cellSize;

        // Draw background
        g.setColor(this.color);
        g.fillRect((int) (xApp - appSize / 2), (int) (yApp - appSize / 2), (int) (appSize), (int) (appSize));

        // Draw borders
        g.setColor(Color.black);
        g.drawLine((int) (xApp - appSize / 2), (int) (yApp - appSize / 2), (int) (xApp + appSize / 2), (int) (yApp - appSize / 2));
        g.drawLine((int) (xApp - appSize / 2), (int) (yApp - appSize / 2), (int) (xApp - appSize / 2), (int) (yApp + appSize / 2));

        int i = 0;
        if (rails != null && !rails.isEmpty()) {
            for (RailSegment railSegment : rails) {
                if (railSegment != null) {
                    railSegment.paint(g, x0, y0, zoom, colorList.get(i % 6));
                }
                i++;
            }
        }
        g.setColor(Color.black);
        String message = "";
        g.drawString(message, (int) (xApp), (int) (yApp));

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
     * @return null if everything went OK, or the train if it could not be
     * reinserted.
     */
    protected TrainElement addTrainElement(TrainElement newTrain) {
        this.trainElements.add(newTrain);
        snapToRail();
        return null;
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

            CardinalPoint leavingDirection = isTrainElementLeaving(trainElement);
            if (leavingDirection != CardinalPoint.CENTER) {
                // The element has travelled to the next cell.
                trainsLeavingCell.add(new TransferringTrain(trainElement, leavingDirection));
            }
        }
        snapToRail();

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
     * Get the heading of the first end of the track, from the outside to the
     * inside of the cell.
     *
     * @return the first track heading
     */
    private double getFirstRailHeading() {
        return rails.get(0).getHeadingInDegrees();
    }

    /**
     * Get the heading of the center of the track.
     *
     * @return the heading of the center of the track
     */
    protected double getCenterHeading() {
        return rails.get(nbRails / 2).getHeadingInDegrees();
    }

    /**
     * Get the heading of the last end of the track, from the outside to the
     * inside of the cell.
     *
     * @return the last track heading
     */
    private double getLastRailHeading() {
        return (rails.get(nbRails - 1).getHeadingInDegrees() + 180) % 360;
    }

    /**
     * Compare two directions and see if they are closer than a margin.
     */
    private boolean compareHeadings(double h0, double h1) {
        double headingDifference = h0 - h1;

        // Need to be close to 0 (or 360) degrees difference.
        boolean result = Math.abs(headingDifference) <= 0 + maxHeadingDiff || Math.abs(headingDifference) >= 360 - maxHeadingDiff;
        return result;
    }

    protected boolean hasLinkTowardNeighbor(double headingTowardNeighbor) {
        if (hasRails()) {
            // Heading of a train leaving this cell via the first end of the tracks.
            double exitFirstHeading = (getFirstRailHeading() + 180) % 360;
            // Heading of a train leaving this cell via the last end of the tracks.
            double exitLastHeading = (getLastRailHeading() + 180) % 360;

            // This cell is headed towards given heading if one of its track ends follows that direction.
            return compareHeadings(exitFirstHeading, headingTowardNeighbor) || compareHeadings(exitLastHeading, headingTowardNeighbor);
        } else {
            return false;
        }
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

    /**
     * If the trainElement has the given trainNumber, that number must be
     * replaced.
     *
     * @param toBeReplaced the old train number; we only renumber trains, not
     * un-linked elements, so we ignore -1
     * @param newTrainNumber
     */
    protected void renumberTrainElement(int toBeReplaced, int newTrainNumber) {
        if (toBeReplaced != -1) {
            for (TrainElement te : trainElements) {
                if (hasTrain() && te.trainNumber == toBeReplaced) {
                    System.out.println("renumbering train element: from " + toBeReplaced + " to " + newTrainNumber);
                    System.out.println("Renamed element " + te.id + " from train " + te.trainNumber
                            + " into " + newTrainNumber);
                    te.trainNumber = newTrainNumber;
                }
            }
        }
    }

    void flushMovingTrains() {
        for (TransferringTrain te : trainsLeavingCell) {
            int index = trainElements.lastIndexOf(te.getTrainElement());
            trainElements.remove(index);
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
            // Find the rail that is closest to the TrainElement
            double minDistance = Double.MAX_VALUE;
            RailSegment closestSegment = null;
            for (RailSegment r : rails) {
                double distance = r.getDistance(te);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestSegment = r;
                }
            }
            if (closestSegment != null) {
                // snap the TrainElement to the rail
                closestSegment.snapTrain(te);
            }
        }
    }

    private String getLinks() {
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
        if (!links.contains(newLinkDirection)) {
            links.add(newLinkDirection);
            if (links.size() > 2) {
                links.remove(0);
            }
        }
        updateTracks();
    }

    private boolean isTrackTurning() {

        return (isLinked(CardinalPoint.NORTH) && isLinked(CardinalPoint.EAST)
                || isLinked(CardinalPoint.EAST) && isLinked(CardinalPoint.SOUTH)
                || isLinked(CardinalPoint.SOUTH) && isLinked(CardinalPoint.WEST)
                || isLinked(CardinalPoint.WEST) && isLinked(CardinalPoint.NORTH));
    }

    /**
     * Set the rail segments in accordance with the cardinal points specified by
     * the link list.
     *
     */
    private void updateTracks() {

        if (isTrackTurning()) {
            updateTurningTracks();
        } else {
            updateHorizontalOrVerticalTracks();
        }
    }

    /**
     * Create the rails when the tracks go N-S or E-W, without turns.
     */
    private void updateHorizontalOrVerticalTracks() {
        double xCell = absolutePosition.x, yCell = absolutePosition.y;
        double xStart = xCell, xEnd = xCell, yStart = yCell, yEnd = yCell;
        if (links.size() >= 1) {
            CardinalPoint firstLink = links.get(0);
            switch (firstLink) {
            case WEST:
                xStart = xCell - cellSize / 2;
                yStart = yCell;
                break;
            case NORTH:
                xStart = xCell;
                yStart = yCell + cellSize / 2;
                break;
            case EAST:
                xStart = xCell + cellSize / 2;
                yStart = yCell;
                break;
            case SOUTH:
                xStart = xCell;
                yStart = yCell - cellSize / 2;
                break;
            default:
                xStart = xCell;
                yStart = yCell;
            }
        }
        if (links.size() >= 2) {
            CardinalPoint secondLink = links.get(1);
            switch (secondLink) {
            case WEST:
                xEnd = xCell - cellSize / 2;
                yEnd = yCell;
                break;
            case NORTH:
                xEnd = xCell;
                yEnd = yCell + cellSize / 2;
                break;
            case EAST:
                xEnd = xCell + cellSize / 2;
                yEnd = yCell;
                break;
            case SOUTH:
                xEnd = xCell;
                yEnd = yCell - cellSize / 2;
                break;
            default:
                xEnd = xCell;
                yEnd = yCell;
            }
        }
        rails.clear();
        for (int i = 0; i < nbRails; i++) {
            double x0 = xStart + i * (xEnd - xStart) / nbRails;
            double y0 = yStart + i * (yEnd - yStart) / nbRails;
            double x1 = xStart + (i + 1) * (xEnd - xStart) / nbRails;
            double y1 = yStart + (i + 1) * (yEnd - yStart) / nbRails;
            rails.add(new RailSegment(x0, y0, x1, y1));
        }
    }

    /**
     * Create the rails when the tracks go from N-S to E-W, with a turn.
     */
    private void updateTurningTracks() {
        double xCenter, yCenter;
        double radius = cellSize / 2;
        double angleStart, angleEnd;

        if (isLinked(CardinalPoint.SOUTH) && isLinked(CardinalPoint.EAST)) {
            xCenter = absolutePosition.x + cellSize / 2;
            yCenter = absolutePosition.y - cellSize / 2;
            angleStart = PI;
            angleEnd = PI / 2;
        } else if (isLinked(CardinalPoint.EAST) && isLinked(CardinalPoint.NORTH)) {
            xCenter = absolutePosition.x + cellSize / 2;
            yCenter = absolutePosition.y + cellSize / 2;
            angleStart = 3 * PI / 2;
            angleEnd = PI;
        } else if (isLinked(CardinalPoint.NORTH) && isLinked(CardinalPoint.WEST)) {
            xCenter = absolutePosition.x - cellSize / 2;
            yCenter = absolutePosition.y + cellSize / 2;
            angleStart = 4 * PI / 2;
            angleEnd = 3 * PI / 2;
        } else { // linked WEST and SOUTH
            xCenter = absolutePosition.x - cellSize / 2;
            yCenter = absolutePosition.y - cellSize / 2;
            angleStart = PI / 2;
            angleEnd = 0;
        }

        rails.clear();
        for (int i = 0; i < nbRails; i++) {
            double x0 = xCenter + radius * cos(angleStart + i * (angleEnd - angleStart) / nbRails);
            double y0 = yCenter + radius * sin(angleStart + i * (angleEnd - angleStart) / nbRails);
            double x1 = xCenter + radius * cos(angleStart + (i + 1) * (angleEnd - angleStart) / nbRails);
            double y1 = yCenter + radius * sin(angleStart + (i + 1) * (angleEnd - angleStart) / nbRails);
            rails.add(new RailSegment(x0, y0, x1, y1));
        }
    }

    /**
     * Return true when the cell has a link in the specified direction.
     *
     * @param cardinalPoint
     * @return
     */
    protected boolean isLinked(CardinalPoint cardinalPoint) {
        try {
            return links.get(0) == cardinalPoint || links.get(1) == cardinalPoint;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    protected void removeTracks() {
        if (!hasTrain()) {
            rails.clear();
        }
    }

    protected void removeTrains() {
        trainElements.clear();
        trainsLeavingCell.clear();
    }
}
