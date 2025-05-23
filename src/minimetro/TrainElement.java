package minimetro;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * This class represents either a locomotive or a passenger carriage.
 *
 * @author arthu
 */
public abstract class TrainElement extends SpriteElement implements ImageObserver {

    protected Point2D.Double absolutePosition;
    protected int row, col; // Position on the grid.
    protected double headingDegrees; // 0:N, 90:E, 180:S, 270:W, MAX_VALUE: not set yet.
    protected double linearSpeed;

    // Highest speed physically achievable.
    protected double maxSpeed;

    // Highest speed legally achievable.
    // -1 for end of limit, >0 for actual limit.
    protected double currentSpeedLimit;

    protected double stopTimerDuration;
    private static double MAX_SPEED_FOR_STOPPED = 0.1;

    protected double mass;
    protected Point2D.Double currentForce;

    protected boolean isEngineActive, isBraking;
    protected double brakingForce = 20.0;

    protected double size; // Physical size of the object
    protected Color color;
    private double spriteZoomLevel;
    protected double spriteWidth, spriteHeight;

    protected int id; // Single value for each element
    protected int trainNumber; // This value is the same for elements linked together; -1 for non-linked elements.
    protected static int NB_TRAIN_ELEMENTS_CREATED = 0;

    /**
     * Create TE with a specific id.
     *
     * @param newId
     */
    public TrainElement(int newId) {
        if (newId == -1) {
            // Default numbering
            id = NB_TRAIN_ELEMENTS_CREATED;
            NB_TRAIN_ELEMENTS_CREATED++;
        } else {
            // Specific numbering
            id = newId;
            NB_TRAIN_ELEMENTS_CREATED = Math.max(NB_TRAIN_ELEMENTS_CREATED, newId + 1);
        }
        trainNumber = -1;
        currentForce = new Point2D.Double();
        absolutePosition = new Point2D.Double();
        headingDegrees = Double.MAX_VALUE;
        linearSpeed = 0;
        size = 3;
        spriteZoomLevel = 0.15;
        mass = 1;
        isEngineActive = true;
        isBraking = false;
        currentSpeedLimit = -1;
        stopTimerDuration = -1;
        spriteWidth = 0;
        spriteHeight = 0;
    }

    /**
     * Create TE with automatic id.
     */
    public TrainElement() {
        this(-1);
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom) {

        super.paint(g, x0, y0, zoom);

        if (image != null) {
            // Paint the image
            spriteWidth = (int) Math.max(5, (image.getWidth(this) * zoom) * spriteZoomLevel);
            spriteHeight = (int) Math.max(5, (image.getHeight(this) * zoom) * spriteZoomLevel);
            Image scaledImage = image.getScaledInstance((int) spriteWidth, -1, Image.SCALE_DEFAULT);

            int xCenter = (int) (x0 + zoom * this.absolutePosition.x);
            int yCenter = (int) (g.getClipBounds().height - (y0 + zoom * this.absolutePosition.y));
            int xImage = (int) (xCenter - spriteWidth / 2);
            int yImage = (int) (yCenter - spriteHeight / 2);

            Graphics2D g2d = (Graphics2D) g;
            double headingRad = degToRad(headingDegrees);

            AffineTransform initGraphicsTransform = g2d.getTransform();
            g2d.rotate(-headingRad, xCenter, yCenter);
            g2d.drawImage(scaledImage, xImage, yImage, (ImageObserver) this);
            g2d.setTransform(initGraphicsTransform);

        } else {
            // Default drawing
            double xCenter = (int) (x0 + zoom * this.absolutePosition.x);
            double yCenter = g.getClipBounds().height - (int) (y0 + zoom * this.absolutePosition.y);
            // Default painting
            int radiusApp = (int) (zoom * size);
            ((Graphics2D) g).setStroke(new BasicStroke());
            g.setColor(this.color);
            g.fillOval((int) (xCenter - radiusApp), (int) (yCenter - radiusApp), 2 * radiusApp, 2 * radiusApp);
            g.setColor(Color.black);
            g.drawOval((int) (xCenter - radiusApp), (int) (yCenter - radiusApp), 2 * radiusApp, 2 * radiusApp);
        }
    }

    public void increaseSpeed(double dSpeed) {
    }

    /**
     * Add the specified force to the total force applied on to this element.
     *
     * @param forceIncrement
     */
    public void increaseForce(Point2D.Double forceIncrement) {
        currentForce.x += forceIncrement.x;
        currentForce.y += forceIncrement.y;
    }

    /**
     * Apply to this element the component of the specified force that is in the
     * same direction as the movement. Only the part of the force that actually
     * changes the speed of the element shall be taken into account.
     *
     * @param forceIncrement
     */
    public void increaseEfficientForce(Point2D.Double forceIncrement) {
        double efficientForce = computeEfficientForce(forceIncrement);

        double ux = cos(getHeadingRad());
        double uy = sin(getHeadingRad());

        currentForce.x += efficientForce * ux;
        currentForce.y += efficientForce * uy;
    }

    private double computeEfficientForce(Point2D.Double f) {
        // unit is the unit vector aligned with the speed of the element.
        double headingRad = degToRad(headingDegrees);

        Point2D.Double unit = new Point2D.Double(Math.cos(headingRad), Math.sin(headingRad));
        double fDotU = f.x * unit.x + f.y * unit.y;
        return fDotU;
    }

    double getHeadingDeg() {
        return headingDegrees;
    }

    double getHeadingRad() {
        return degToRad(headingDegrees);
    }

    void setHeadingDegrees(double newHeading) {
        if (newHeading > 360) {
            newHeading -= 360;
        }
        headingDegrees = newHeading;
    }

    private void paintSpeed(Graphics g, int xCenter, int yCenter, int size) {
        g.setColor(Color.white);
        double headingRad = getHeadingRad();
        double vx = linearSpeed * cos(headingRad);
        double vy = linearSpeed * sin(headingRad);
        int scale = 100;
        g.drawLine(xCenter, yCenter, (int) (xCenter + scale * vx), (int) (yCenter - scale * vy));
    }

    protected void paintForce(Graphics g, int xCenter, int yCenter, double zoom) {
        g.setColor(Color.red);
        double fx = this.currentForce.x;
        double fy = this.currentForce.y;
        double scale = zoom;
        g.drawLine(xCenter, yCenter, (int) (xCenter + scale * fx), (int) (yCenter - scale * fy));
    }

    void resetForces() {
        currentForce = new Point2D.Double();
    }

    void computeMotorForce(double dt) {
        currentForce = new Point2D.Double();
    }

    /**
     * Compute the effect of the current force on the speed.
     *
     * @param dt
     */
    protected void computeNewSpeed(double dt) {

        // Force along the axis of the TrainElement
        double efficientForce = computeEfficientForce(currentForce);
        linearSpeed += efficientForce * dt / mass;
    }

    protected void setSpeed(double newVx, double newVy) {
        linearSpeed = sqrt(newVx * newVx + newVy * newVy);
        double newHeadingRad = Math.atan2(newVy, newVx);
        headingDegrees = radToDeg(newHeadingRad);
    }

    void move(double dt) {
        Point2D.Double movement = new Point2D.Double(getVx() * dt, getVy() * dt);
        absolutePosition = new Point2D.Double(absolutePosition.x + movement.x, absolutePosition.y + movement.y);
        if (stopTimerDuration > 0 && getLinearSpeed() < MAX_SPEED_FOR_STOPPED) {
            stopTimerDuration -= dt;
        }
    }

    double getX() {
        return absolutePosition.x;
    }

    double getY() {
        return absolutePosition.y;
    }

    protected void setPosition(double newX, double newY) {
        this.absolutePosition = new Point2D.Double(newX, newY);
    }

    protected double getLinearSpeed() {
        return linearSpeed;
    }

    protected boolean isStopped() {
        return Math.abs(getLinearSpeed()) < MAX_SPEED_FOR_STOPPED;
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return false;
    }

    protected abstract void start();

    protected abstract void stop();

    /**
     * Set a new speed limit or remove the previous limit.
     *
     * @param newSpeedLimit -1 for end of limit, >0 for actual limit.
     */
    protected void setSpeedLimit(double newSpeedLimit) {
        currentSpeedLimit = newSpeedLimit;
    }

    /**
     * Make sure the train slows down if it is travelling too fast.
     *
     * Use the value of currentSpeedLimit:
     * -1 for end of limit, >0 for actual limit.
     */
    protected void observeCurrentSpeedLimit() {
        if (currentSpeedLimit >= 0 && getLinearSpeed() > currentSpeedLimit) {
            isBraking = true;
        } else {
            isBraking = false;
        }
    }

    protected void setTimedStop(double newStopTimerDuration) {
        stopTimerDuration = newStopTimerDuration;
    }

    protected double degToRad(double headingDegrees) {
        return ((90 - headingDegrees) * 2 * PI) / 360;
    }

    private double radToDeg(double headingRad) {
        return 90 - headingRad * 360 / (2 * PI);
    }

    protected double getVx() {
        return linearSpeed * cos(getHeadingRad());
    }

    protected double getVy() {
        return linearSpeed * sin(getHeadingRad());
    }

    protected void addStationToLine(StationCell newStation) {
        World.map.addStation(newStation, this.id);
    }

    private String keep2Decimals(String s) {
        int rankOfDot = s.indexOf('.');
        if (rankOfDot == -1) {
            // No dot, return parameter unchanged
            return s;
        } else {
            // Trim after 2 decimals
            String trimmed = s.substring(0, Math.min(s.length(), rankOfDot + 3));
            return trimmed;
        }
    }

    @Override
    public String toString() {
        String elemClass = "" + getClass();
        elemClass = elemClass.substring(elemClass.indexOf(".") + 1);
        return elemClass
                + " " + this.id
                + keep2Decimals(" " + this.absolutePosition.x)
                + keep2Decimals(" " + this.absolutePosition.y)
                + keep2Decimals(" " + this.headingDegrees)
                + keep2Decimals(" " + this.linearSpeed)
                + keep2Decimals(" " + this.currentSpeedLimit);
    }

    protected void save(FileWriter writer) {
        try {
            String text = toString();
            writer.write(text + "\n");
        } catch (IOException ex) {
            System.out.println("Error writing TrainElement to file");
        }
    }

    void setLinearSpeed(double newLinearSpeed) {
        linearSpeed = newLinearSpeed;
    }

    protected void setRow(int newRow) {
        this.row = newRow;
    }

    protected void setCol(int newCol) {
        this.col = newCol;
    }

    protected int getRow() {
        return this.row;
    }

    protected int getCol() {
        return this.col;
    }

}
