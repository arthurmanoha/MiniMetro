package minimetro;

import java.awt.Color;
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

    protected Color color;

    protected int id; // Single value for each element
    protected int trainNumber; // This value is the same for elements linked together.
    protected static int NB_TRAIN_ELEMENTS_CREATED = 0;

    public TrainElement() {
        id = NB_TRAIN_ELEMENTS_CREATED;
        NB_TRAIN_ELEMENTS_CREATED++;
        trainNumber = id;
    }

    public void paint(Graphics g, int xApp, int yApp, int size, Point2D.Double elementPosition, double heading) {
        int xCenter = (int) (xApp + size * elementPosition.x);
        int yCenter = (int) (yApp + size * elementPosition.y);
        int radius = (int) (size * 0.10);
        g.setColor(this.color);
        g.fillOval((int) (xCenter - radius), (int) (yCenter - radius), 2 * radius, 2 * radius);
        g.setColor(Color.black);
        g.drawOval((int) (xCenter - radius), (int) (yCenter - radius), 2 * radius, 2 * radius);

        // Paint id and trainNumber
        g.setColor(Color.black);
        g.drawString("id " + id + ", tn " + trainNumber + ", h " + headingDegrees,
                 xCenter - 2 * radius, yCenter - 2 * radius
        );
    }

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
