package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

/**
 *
 * @author arthu
 */
public class Locomotive extends TrainElement {

    protected double motorPower;

    public Locomotive() {
        super();
        maxSpeed = 1;
        motorPower = 1;
        color = Color.red;
    }

    public Locomotive(Point2D.Double newAbsolutePosition) {
        this();
        this.absolutePosition = newAbsolutePosition;
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom) {
        super.paint(g, x0, y0, zoom);
    }

    @Override
    public void increaseSpeed(double dSpeed) {

    }

    @Override
    public void computeMotorForce(double dt) {

        double dx = Math.cos(getHeadingRad());
        double dy = Math.sin(getHeadingRad());

        double fx = motorPower * dx;
        double fy = motorPower * dy;
        currentForce = new Point2D.Double(fx, fy);
    }
}
