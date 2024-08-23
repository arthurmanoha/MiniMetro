package minimetro;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import static java.lang.Math.PI;

/**
 * This class represents either a locomotive or a passenger carriage.
 *
 * @author arthu
 */
public abstract class TrainElement extends SpriteElement implements ImageObserver {

    protected Point2D.Double absolutePosition;
    protected Point2D.Double currentSpeed;
    protected double maxSpeed;
    protected double mass;
    protected Point2D.Double currentForce;
    protected double headingDegrees; // 0:N, 90:E, 180:S, 270:W

    protected double size; // Physical size of the object

    // Only the first element of a given train is responsible of speed computation;
    protected boolean isLeading;

    protected Color color;
    private int spriteZoomLevel = 600;

    protected int id; // Single value for each element
    protected int trainNumber; // This value is the same for elements linked together; -1 for non-linked elements.
    protected static int NB_TRAIN_ELEMENTS_CREATED = 0;

    public TrainElement() {
        id = NB_TRAIN_ELEMENTS_CREATED;
        NB_TRAIN_ELEMENTS_CREATED++;
        trainNumber = -1;
        isLeading = false;
        currentForce = new Point2D.Double();
        absolutePosition = new Point2D.Double();
        currentSpeed = new Point2D.Double();
        size = 0.03;
        mass = 1;
    }

    @Override
    public String toString() {
        return "TE_" + id;
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom) {

        super.paint(g, x0, y0, zoom);

        if (image != null) {
            // Paint the image
            int newWidth = (int) Math.max(5, (image.getWidth(this) * zoom) / spriteZoomLevel);
            int newHeight = (int) Math.max(5, (image.getHeight(this) * zoom) / spriteZoomLevel);
            Image scaledImage = image.getScaledInstance(newWidth, -1, Image.SCALE_DEFAULT);

            int xCenter = (int) (x0 + zoom * this.absolutePosition.x);
            int yCenter = (int) (g.getClipBounds().height - (y0 + zoom * this.absolutePosition.y));
            int xImage = xCenter - newWidth / 2;
            int yImage = yCenter - newHeight / 2;

            Graphics2D g2d = (Graphics2D) g;
            double headingRad = ((90 - headingDegrees) * 2 * PI) / 360;

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
        Point2D.Double efficientForce = computeEfficientForce(forceIncrement);
        currentForce.x += efficientForce.x;
        currentForce.y += efficientForce.y;
    }

    private Point2D.Double computeEfficientForce(Point2D.Double f) {
        // unit is the unit vector aligned with the speed of the element.
        double headingRad = (PI / 2) * (90 - headingDegrees) / 90;

        Point2D.Double unit = new Point2D.Double(Math.cos(headingRad), Math.sin(headingRad));
        double fDotU = f.x * unit.x + f.y * unit.y;
        Point2D.Double efficientForce = new Point2D.Double(fDotU * unit.x, fDotU * unit.y);
        return efficientForce;
    }

    double getHeading() {
        return headingDegrees;
    }

    double getHeadingRad() {
        return (2 * PI * (90 - headingDegrees)) / 360;
    }

    void setHeadingDegrees(double newHeading) {
        if (newHeading > 360) {
            newHeading -= 360;
        }
        headingDegrees = newHeading;
    }

    private void paintSpeed(Graphics g, int xCenter, int yCenter, int size) {
        g.setColor(Color.white);
        double vx = this.currentSpeed.x;
        double vy = this.currentSpeed.y;
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

    void computeNewSpeed(double dt) {
        currentSpeed.x += currentForce.x * dt / mass;
        currentSpeed.y += currentForce.y * dt / mass;
    }

    void move(double dt) {
        Point2D.Double movement = new Point2D.Double(currentSpeed.x * dt, currentSpeed.y * dt);
        absolutePosition = new Point2D.Double(absolutePosition.x + movement.x, absolutePosition.y + movement.y);
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

    protected boolean isStopped() {
        double vx = currentSpeed.x;
        double vy = currentSpeed.y;
        return Math.abs(vx) + Math.abs(vy) < 0.01;
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return false;
    }
}
