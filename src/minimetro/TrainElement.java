package minimetro;

import java.awt.Color;
import java.awt.Graphics;

/**
 * This class represents either a locomotive or a passenger carriage.
 *
 * @author arthu
 */
public abstract class TrainElement {

    protected double x, y; // Position in a given cell.
    protected double vx, vy; // Speed relative to the cell.
    protected double maxSpeed;
    protected double headingDegrees; // 0:N, 90:E, 180:S, 270:W

    protected Color color;

    protected int id; // Single value for each element
    protected int trainNumber; // This value is the same for elements linked together.
    protected static int NB_TRAIN_ELEMENTS_CREATED = 0;

    public TrainElement(double newX, double newY, double newVx, double newVy) {
        id = NB_TRAIN_ELEMENTS_CREATED;
        NB_TRAIN_ELEMENTS_CREATED++;
        trainNumber = id;
        x = newX;
        y = newY;
        vx = newVx;
        vy = newVy;
    }

    public TrainElement(double newX, double newY) {
        this(newX, newY, 0, 0);
    }

    public void paint(Graphics g, int xApp, int yApp, int size) {
        int xCenter = (int) (xApp + size * x);
        int yCenter = (int) (yApp + size * y);
        int radius = (int) (size * 0.10);
        g.setColor(this.color);
        g.fillOval((int) (xCenter - radius), (int) (yCenter - radius), 2 * radius, 2 * radius);
        g.setColor(Color.black);
        g.drawOval((int) (xCenter - radius), (int) (yCenter - radius), 2 * radius, 2 * radius);

        // Paint id and trainNumber
        g.setColor(Color.black);
        g.drawString("id " + id
                + ", tn " + trainNumber
                + ", h " + headingDegrees
                + ", v{" + vx + ", " + vy + "}",
                xCenter - 2 * radius, yCenter - 2 * radius
        );
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
