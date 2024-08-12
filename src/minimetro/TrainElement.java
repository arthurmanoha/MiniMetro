package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import static java.lang.Math.PI;

/**
 * This class represents either a locomotive or a passenger carriage.
 *
 * @author arthu
 */
public abstract class TrainElement {

    protected Point2D.Double absolutePosition;
    protected Point2D.Double currentSpeed;
    protected double maxSpeed;
    protected Point2D.Double currentForce;
    protected double headingDegrees; // 0:N, 90:E, 180:S, 270:W

    protected double size; // Physical size of the object

    // Only the first element of a given train is responsible of speed computation;
    protected boolean isLeading;

    protected Color color;

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
        size = 0.1;
    }

    public void paint(Graphics g, double x0, double y0, double zoom) {
        int xCenter = (int) (x0 + zoom * this.absolutePosition.x);
        int yCenter = g.getClipBounds().height - (int) (y0 + zoom * this.absolutePosition.y);
        int radiusApp = (int) (zoom * size);
        g.setColor(this.color);
        g.fillOval((int) (xCenter - radiusApp), (int) (yCenter - radiusApp), 2 * radiusApp, 2 * radiusApp);
        g.setColor(Color.black);
        g.drawOval((int) (xCenter - radiusApp), (int) (yCenter - radiusApp), 2 * radiusApp, 2 * radiusApp);

        // Paint id, trainNumber and heading
        g.setColor(Color.black);
        String headingSubstring = (int) headingDegrees + "";
        String linearVelocity = currentSpeed + "";
        g.drawString("id " + id
                + ", h " + headingSubstring
                + ", v " + linearVelocity,
                xCenter - 2 * radiusApp, yCenter - 2 * radiusApp
        );
    }

    public void increaseSpeed(double dSpeed) {
    }

    public void increaseForce(Point2D.Double forceIncrement) {
        currentForce.x += forceIncrement.x;
        currentForce.y += forceIncrement.y;
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

    void resetForces() {
        currentForce = new Point2D.Double();
    }

    void computeMotorForce(double dt) {
        currentForce = new Point2D.Double();
    }

    void computeNewSpeed(double dt) {

        currentSpeed.x += currentForce.x * dt;
        currentSpeed.y += currentForce.y * dt;

        double speed = Math.sqrt(currentSpeed.x * currentSpeed.x + currentSpeed.y * currentSpeed.y);
        if (speed > maxSpeed) {
            double ratio = speed / maxSpeed;
            currentSpeed.x = currentSpeed.x / ratio;
            currentSpeed.y = currentSpeed.y / ratio;
        }
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
}
