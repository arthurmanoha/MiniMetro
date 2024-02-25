package minimetro;

import java.awt.Graphics;
import java.awt.geom.Point2D;

/**
 * This class represents either a locomotive or a passenger carriage.
 *
 * @author arthu
 */
public abstract class TrainElement {

    protected double position;
    protected double currentSpeed; // sign depends on train direction on local track.
    protected double maxSpeed;
    protected double headingDegrees; // 0:N, 90:E, 180:S, 270:W

    public abstract void paint(Graphics g, int xApp, int yApp, int size, Point2D.Double elementPosition, double heading);

    public void increasePosition(double dPos) {
        this.position += dPos;
    }

    protected double getPosition() {
        return position;
    }

    protected void setPosition(double newPosition) {
        position = newPosition;
    }

    double getHeading() {
        return headingDegrees;
    }

    void setHeadingDegrees(double newHeading) {
        if (newHeading > 360) {
            newHeading -= 360;
        }
        headingDegrees = newHeading;
    }
}
