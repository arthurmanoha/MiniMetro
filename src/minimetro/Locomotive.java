package minimetro;

import java.awt.Color;
import java.awt.Font;
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
        maxSpeed = 60.0;
        motorPower = 5.0;
        color = Color.red;
        mass = 10;
        imagePath = "src\\img\\Locomotive.png";
        loadImage();
    }

    public Locomotive(Point2D.Double newAbsolutePosition) {
        this();
        this.absolutePosition = newAbsolutePosition;
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom) {
        super.paint(g, x0, y0, zoom);
        g.setColor(Color.black);

        double linearSpeed = getLinearSpeed();
        String linearSpeedText = linearSpeed + "";
        if (linearSpeed > 0.0001) {
            int rankOfDot = linearSpeedText.indexOf(".");
            if (rankOfDot != -1) {
                linearSpeedText = linearSpeedText.substring(0, Math.min(linearSpeedText.length(), rankOfDot + 3));
            }
        } else {
            linearSpeedText = "0";
        }
        String text = "" + linearSpeedText + (isBraking ? " _" : "") + " - limit: " + currentSpeedLimit;
        int xCenter = (int) (x0 + zoom * this.absolutePosition.x);
        int yCenter = (int) (g.getClipBounds().height - (y0 + zoom * this.absolutePosition.y));
        g.setColor(Color.black);
        g.setFont(new Font("helvetica", Font.PLAIN, 15));
        g.drawString(text, xCenter + 20 * (int) zoom, yCenter + 20 * (int) zoom);
    }

    @Override
    public void increaseSpeed(double dSpeed) {

    }

    /**
     * When the loco is pulling, its force is non-zero.
     *
     * @param dt
     */
    @Override
    public void computeMotorForce(double dt) {

        double dx = Math.cos(getHeadingRad());
        double dy = Math.sin(getHeadingRad());
        double fx = 0, fy = 0;

        if (isBraking) {
            fx += -brakingForce * currentSpeed.x;
            fy += -brakingForce * currentSpeed.y;
        } else if (isEngineActive) { // Deactivate engine upon brake activation.
            fx += motorPower * dx;
            fy += motorPower * dy;
        }
        currentForce = new Point2D.Double(fx, fy);
    }

    @Override
    void computeNewSpeed(double dt) {

        super.computeNewSpeed(dt);

        // Limit the speed.
        double speed = Math.sqrt(currentSpeed.x * currentSpeed.x + currentSpeed.y * currentSpeed.y);
        if (speed > maxSpeed) {
            double ratio = speed / maxSpeed;
            currentSpeed.x = currentSpeed.x / ratio;
            currentSpeed.y = currentSpeed.y / ratio;
        }
    }

    @Override
    protected void start() {
        isEngineActive = true;
        isBraking = false;
        System.out.println("Loco " + id + " started");
    }

    @Override
    protected void stop() {
        isEngineActive = false;
        isBraking = true;
        System.out.println("Loco " + id + " stopped");
    }
}
