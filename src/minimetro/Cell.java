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
 * It may contain one piece of track, one carriage.
 *
 * @author arthu
 */
public class Cell {

    protected Color color;

    private TrainElement trainElement;
    protected boolean isTrainElementSwitchingCells;
    double maxHeadingDiff = 10;

    private ArrayList<RailSegment> rails;
    private int nbRails;
    private double totalRailLength;
    private double singleRailLength;
    ArrayList<Color> colorList;
    private boolean trainIsPrograde;

    public Cell(ArrayList<RailSegment> oldRails) {
        color = Color.gray;
        trainElement = null;
        if (oldRails == null) {
            rails = new ArrayList<>();
            nbRails = 10;
        } else {
            rails = oldRails;
            nbRails = rails.size();
        }

        totalRailLength = 1.0;

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
        isTrainElementSwitchingCells = false;
    }

    public Cell() {
        this(null);
    }

    /**
     * Paint the cell with its background and foreground.
     */
    protected void paint(Graphics g, int row, int col, double x0, double y0, double zoom) {
        final int xApp = (int) (col * zoom + x0);
        final int yApp = (int) (row * zoom + y0);

        // Draw background
        g.setColor(this.color);
        g.fillRect(xApp, yApp, (int) zoom + 1, (int) zoom + 1);

        // Draw borders
        g.setColor(Color.black);
        g.drawLine(xApp, yApp, (int) (xApp + zoom), yApp);
        g.drawLine(xApp, yApp, xApp, (int) (yApp + zoom));

        int i = 0;
        if (rails != null) {
            for (RailSegment railSegment : rails) {
                railSegment.paint(g, xApp, yApp, zoom, colorList.get(i % 6));
                i++;
            }
        }
    }

    /**
     * Paint the trainElements alone.
     * (to be used after all cells were drawn).
     */
    protected void paintTrains(Graphics g, int row, int col, double x0, double y0, double zoom) {
        if (hasTrain()) {
            final int xApp = (int) (col * zoom + x0);
            final int yApp = (int) (row * zoom + y0);

            // Compute the position based on the rails.
            Point2D.Double elementPosition = getTrainPosition();
            double heading = getTrainHeading();
            if (elementPosition != null) {
                trainElement.paint(g, xApp, yApp, (int) zoom, elementPosition, heading);
            }
        }
    }

    /**
     * Set a track section that goes through this cell.
     *
     */
    protected void setTrack(int dxBefore, int dyBefore, int dxAfter, int dyAfter) {
        double xStart = defineBorderCoordinates(dxBefore);
        double xEnd = defineBorderCoordinates(dxAfter);
        double yStart = defineBorderCoordinates(dyBefore);
        double yEnd = defineBorderCoordinates(dyAfter);

        rails.clear();

        // Create rail sections
        if (dxBefore != dxAfter && dyBefore != dyAfter) {
            // This is a turn.
            double xCenter, yCenter;
            double angleStart, angleEnd;
            double radius = 0.5;
            if (dxBefore > 0 && dyAfter > 0 || dxAfter > 0 && dyBefore > 0) {
                // Link between North and East.
                xCenter = 1;
                yCenter = 1;
                // 0<->east, pi/2<->south, pi<->west, 3pi/2<->north.
                angleStart = PI;
                angleEnd = 3 * PI / 2;
            } else if (dxBefore > 0 && dyAfter < 0 || dxAfter > 0 && dyBefore < 0) {
                // Link between East and South.
                xCenter = 1;
                yCenter = 0;
                // 0<->east, pi/2<->south, pi<->west, 3pi/2<->north.
                angleStart = PI / 2;
                angleEnd = PI;
            } else if (dxBefore < 0 && dyAfter < 0 || dxAfter < 0 && dyBefore < 0) {
                // Link between South and West.
                xCenter = 0;
                yCenter = 0;
                // 0<->east, pi/2<->south, pi<->west, 3pi/2<->north.
                angleStart = 0;
                angleEnd = PI / 2;
            } else if (dxBefore < 0 && dyAfter > 0 || dxAfter < 0 && dyBefore > 0) {
                // Link between West and North.
                xCenter = 0;
                yCenter = 1;
                // 0<->east, pi/2<->south, pi<->west, 3pi/2<->north.
                angleStart = 3 * PI / 2;
                angleEnd = 2 * PI;
            } else {
                System.out.println("Cell.setTrack: Error in quadrant selection.");
                angleStart = 0;
                angleEnd = 0;
                xCenter = 0;
                yCenter = 0;
            }
            for (int i = 0; i < nbRails; i++) {

                double percentageInit = (double) i / nbRails;
                double currentAngle = angleStart * (1 - percentageInit) + angleEnd * percentageInit;
                double xSectionStart = xCenter + radius * cos(currentAngle);
                double ySectionStart = yCenter + radius * sin(currentAngle);
                double percentageEnd = (double) (i + 1) / nbRails;
                double nextAngle = angleStart * (1 - percentageEnd) + angleEnd * percentageEnd;
                double xSectionEnd = xCenter + radius * cos(nextAngle);
                double ySectionEnd = yCenter + radius * sin(nextAngle);
                rails.add(new RailSegment(xSectionStart, ySectionStart, xSectionEnd, ySectionEnd));
            }
        } else {
            // This is a straight line.
            for (int i = 0; i < nbRails; i++) {
                double percentageStart = (double) i / nbRails;
                double xSectionStart = xStart * (1 - percentageStart) + xEnd * percentageStart;
                double ySectionStart = yStart * (1 - percentageStart) + yEnd * percentageStart;
                double percentageEnd = ((double) (i + 1)) / nbRails;
                double xSectionEnd = xStart * (1 - percentageEnd) + xEnd * percentageEnd;
                double ySectionEnd = yStart * (1 - percentageEnd) + yEnd * percentageEnd;
                rails.add(new RailSegment(xSectionStart, ySectionStart, xSectionEnd, ySectionEnd));
            }
        }
    }

    /**
     * return a value of 0, 0.5 or 1 depending on where the neighbor cell is
     * located.
     *
     * @param dxy
     * @return
     */
    private double defineBorderCoordinates(double dxy) {
        if (dxy == -1) {
            return 0;
        } else if (dxy == 0) {
            return 0.5;
        } else {
            return 1;
        }
    }

    // A cell has tracks if it is linked to at least one neighbor.
    protected boolean hasRails() {
        return !rails.isEmpty();
    }

    protected void setLoco() {
        if (hasRails()) {
            this.trainElement = new Locomotive();
            this.trainIsPrograde = true;
            this.trainElement.headingDegrees = this.getCenterHeading();
        }
    }

    protected void setWagon() {
        if (hasRails()) {
            this.trainElement = new Wagon();
            this.trainIsPrograde = true;
            this.trainElement.headingDegrees = this.getCenterHeading();
        }
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

        if (!hasRails()) {
            System.out.println("No rails, cannot add train.");
            return newTrain;
        }

        if (this.compareFirstInputHeading(newTrain.headingDegrees)) {
            // Insert train in first rail segment.
            trainElement = newTrain;
            this.trainIsPrograde = true;
        } else if (this.compareLastInputHeading(newTrain.headingDegrees)) {
            // Insert train in last rail segment.
            trainElement = newTrain;
            this.trainIsPrograde = false;
        } else {
            return newTrain;
        }
        return null;
    }

    protected boolean hasLoco() {
        return this.trainElement != null && (this.trainElement instanceof Locomotive);
    }

    protected boolean hasWagon() {
        return this.trainElement != null && (this.trainElement instanceof Wagon);
    }

    protected boolean hasTrain() {
        return hasLoco() || hasWagon();
    }

    protected Locomotive getLoco() {
        if (hasLoco()) {
            return (Locomotive) this.trainElement;
        } else {
            return null;
        }
    }

    /**
     * Remove and return the trainElement if it exists, null otherwise.
     *
     * @return the trainElement if it exists, null otherwise.
     */
    protected TrainElement removeTrain() {
        if (this.hasTrain()) {
            TrainElement removedTrainElement = this.trainElement;
            this.trainElement = null;
            return removedTrainElement;
        } else {
            return null;
        }
    }

    protected void evolve(double dt) {
        if (hasTrain()) {
            double dPos = trainElement.currentSpeed * dt; // The absolute distance the train will move in this step.
            trainElement.increasePosition(dPos);
            if (trainElement.getPosition() > 1 || trainElement.getPosition() < 0) {
                // The element has travelled to the next cell.
                isTrainElementSwitchingCells = true;
            } else {
                setTrainHeading();
            }
        }

    }

    /**
     * Compute the 2d position of the train from its linear position and the
     * tracks details.
     *
     * @return the 2d position relative to the current cell.
     */
    private Point2D.Double getTrainPosition() {

        double cellPos; // Train position in the cell's reference (swapped if train drives retrograde)
        if (trainIsPrograde) {
            cellPos = trainElement.position;
        } else {
            cellPos = 1 - trainElement.position;
        }
        int railIndex = (int) (cellPos / totalRailLength * nbRails);
        double lengthBeforeCurrentSegment;
        lengthBeforeCurrentSegment = railIndex * singleRailLength;
        double p = (cellPos - lengthBeforeCurrentSegment) / singleRailLength;

        if (railIndex >= nbRails || railIndex < 0) {
            // The train has reached the border of the cell.
            return null;
        }
        double xStart = rails.get(railIndex).getXStart();
        double yStart = rails.get(railIndex).getYStart();
        double xEnd = rails.get(railIndex).getXEnd();
        double yEnd = rails.get(railIndex).getYEnd();
        return new Point2D.Double(xStart * (1 - p) + xEnd * p, yStart * (1 - p) + yEnd * p);
    }

    /**
     * Compute the heading of the train from its linear position and the
     * tracks details.
     *
     * @return the train heading in radians, 0<->east, pi/2<->south, pi<->west,
     * 3pi/2<->north.
     */
    private double getTrainHeading() {
        return trainElement.getHeading();
    }

    private void setTrainHeading() {

        int railIndex = -1;
        try {
            double trainPosition = trainElement.position;

            if (trainIsPrograde) {
                railIndex = (int) (trainPosition * nbRails / totalRailLength);
            } else {
                railIndex = (int) ((1 - trainPosition) * nbRails / totalRailLength);
            }

            RailSegment segment = rails.get(railIndex);
            double segmentHeading = segment.getHeadingInDegrees();
            double newHeading = segmentHeading;

            // Negative speed: other direction
            if (trainElement.currentSpeed < 0) {
                newHeading += 180;
            }

            // Train going to the negative direction in the cell's perspective:
            if (!trainIsPrograde) {
                newHeading += 180;
            }

            newHeading = newHeading % 360;

            trainElement.setHeadingDegrees(newHeading);

        } catch (IndexOutOfBoundsException e) {
            System.out.println("no rail numbered " + railIndex);
        }
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
     * Compare the heading of an incoming train to the first side of the tracks,
     * with margin.
     */
    private boolean compareFirstInputHeading(double heading) {
        return compareHeadings(heading, getFirstRailHeading());
    }

    /**
     * Compare the heading of an incoming train to the second side of the
     * tracks, with margin.
     */
    private boolean compareLastInputHeading(double heading) {
        return compareHeadings(heading, getLastRailHeading());
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
        return this.trainElement;
    }

    /**
     * If the trainElement has the given trainNumber, that number must be
     * replaced.
     *
     * @param toBeReplaced
     * @param newTrainNumber
     */
    protected void renumberTrainElement(int toBeReplaced, int newTrainNumber) {
        if (hasTrain() && trainElement.trainNumber == toBeReplaced) {
            System.out.println("renumbering train element: from " + toBeReplaced + " to " + newTrainNumber);
            System.out.println("Renamed element " + trainElement.trainNumber
                    + " into " + newTrainNumber);
            trainElement.trainNumber = newTrainNumber;
        }
    }
}
