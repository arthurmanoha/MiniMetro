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

    public Cell(ArrayList<RailSegment> oldRails) {
        color = Color.gray;
        trainElement = null;
        if (oldRails == null) {
            rails = new ArrayList<>();
            nbRails = 9;
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
        if (hasLoco()) {
            final int xApp = (int) (col * zoom + x0);
            final int yApp = (int) (row * zoom + y0);

            // Compute the position based on the rails.
            Point2D.Double elementPosition = getTrainPosition();
            double heading = getTrainHeading();
            if (elementPosition != null) {
                ((Locomotive) trainElement).paint(g, xApp, yApp, (int) zoom, elementPosition, heading);
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
            newTrain.setPosition(0.05);
            newTrain.setTravellingPositive(true);
            System.out.println("add train travelling positive");
        } else if (this.compareLastInputHeading(newTrain.headingDegrees)) {
            // Insert train in last rail segment.
            trainElement = newTrain;
            newTrain.setPosition(0.95);
            newTrain.setTravellingPositive(false);
            System.out.println("add train travelling negative");
        }
        this.trainElement = newTrain;
        return null;
    }

    protected boolean hasLoco() {
        return this.trainElement != null && (this.trainElement instanceof Locomotive);
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
        if (this.hasLoco()) {
            Locomotive loco = this.getLoco();
            this.trainElement = null;
            return loco;
        } else {
            return null;
        }
    }

    protected void setWagon() {
        if (hasRails()) {
            this.trainElement = new Wagon();
        }
    }

    protected void evolve(double dt) {
        if (hasLoco() || hasRails()) {
//            System.out.println("cell.evolve(" + dt + ")");
            if (hasLoco()) {
                trainElement.evolve(dt);
                if (trainElement.getPosition() > 1 || trainElement.getPosition() < 0) {
                    // The element has travelled to the next cell.
                    isTrainElementSwitchingCells = true;
                } else {
                    setTrainHeading();
                }
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

        double trainPosition = trainElement.position;
        int railIndex = (int) (trainPosition * nbRails / totalRailLength);
//        System.out.println("rail index: " + railIndex);
        // Percentage along the rail segment:
        double lengthBeforeCurrentSegment = (railIndex - 1) * singleRailLength;
//        System.out.println("length before: " + lengthBeforeCurrentSegment);
        double p = (trainPosition - lengthBeforeCurrentSegment) / singleRailLength;
//        System.out.println("p = " + p);
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
            railIndex = (int) (trainPosition * nbRails / totalRailLength);
            RailSegment segment = rails.get(railIndex);
            double heading = segment.getHeadingInDegrees();
            System.out.println("Train speed: " + trainElement.currentSpeed);
            if (trainElement.currentSpeed > 0) {
                trainElement.setHeadingDegrees(heading);
            } else {
                trainElement.setHeadingDegrees(heading + 180);
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("no rail numbered " + railIndex);
        }
    }

    // Allow for a pi/4 difference when rails are always straight within a cell.
    // When the rails are bent, the max difference shall be smaller (i.e. the first segment
    // will be more aligned)
    private boolean compareFirstInputHeading(double heading) {
        double entryRailHeading = rails.get(0).getHeadingInDegrees(); // The direction of the "start" of the rails.
        double headingDifference = entryRailHeading - heading;
        boolean result = Math.abs(headingDifference) <= maxHeadingDiff;
        System.out.println("compareFirstInputHeading(" + heading + " and " + rails.get(0).getHeadingInDegrees() + "): " + headingDifference + ", max: " + maxHeadingDiff + ", decision: " + result);
        return result;
    }

    private boolean compareLastInputHeading(double heading) {
        double entryRailHeading = (rails.get(nbRails - 1).getHeadingInDegrees() + 180) % 360; // The opposite direction of the "end" of the rails.
        double headingDifference = entryRailHeading - heading;

        // Need to be close to 0 (or 360) degrees difference.
        boolean result = Math.abs(headingDifference) <= 0 + maxHeadingDiff || Math.abs(headingDifference) >= 360 - maxHeadingDiff;
        System.out.println("compareLastInputHeading(" + heading + " and " + rails.get(nbRails - 1).getHeadingInDegrees() + "): "
                + headingDifference + ", max: " + maxHeadingDiff + ", decision: " + result);
        return result;
    }

}
