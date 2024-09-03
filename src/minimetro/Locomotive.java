package minimetro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import static java.lang.Math.abs;

/**
 *
 * @author arthu
 */
public class Locomotive extends TrainElement {

    protected double motorPower;

    public Locomotive() {
        super();
        maxSpeed = 300.0;
        motorPower = 30.0;
        color = Color.red;
        mass = 10;
        imagePath = "src\\img\\Locomotive.png";
        loadImage();
    }

    public Locomotive(Point2D.Double newAbsolutePosition) {
        this();
//        System.out.println(newAbsolutePosition.x + ", " + newAbsolutePosition.y);
        this.absolutePosition = newAbsolutePosition;
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom) {
        super.paint(g, x0, y0, zoom);
        g.setColor(Color.black);

        String linearSpeedText = linearSpeed + "";
        if (abs(linearSpeed) > 0.001) {
            int rankOfDot = linearSpeedText.indexOf(".");
            if (rankOfDot != -1) {
                linearSpeedText = linearSpeedText.substring(0, Math.min(linearSpeedText.length(), rankOfDot + 3));
            }
        } else {
            linearSpeedText = "0";
        }
        String text = "v " + linearSpeedText;
        if (stopTimerDuration > 0) {
            text += (" STOP " + (int) stopTimerDuration);
        }
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

        if (isBraking || stopTimerDuration > 0) {
            fx += -brakingForce * getVx();
            fy += -brakingForce * getVy();
        } else if (isEngineActive) { // Deactivate engine upon brake activation.
            fx += motorPower * dx;
            fy += motorPower * dy;
        }
        currentForce = new Point2D.Double(fx, fy);
    }

    @Override
    protected void computeNewSpeed(double dt) {

        super.computeNewSpeed(dt);

        // Limit the speed.
        if (linearSpeed > maxSpeed) {
            double ratio = linearSpeed / maxSpeed;
            linearSpeed = linearSpeed / ratio;
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
