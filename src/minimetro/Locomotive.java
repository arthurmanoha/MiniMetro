package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

/**
 *
 * @author arthu
 */
public class Locomotive extends TrainElement {

    public Locomotive() {
        position = 0.5;
        currentSpeed = 1;
        maxSpeed = 1;
    }

    @Override
    public void paint(Graphics g, int xApp, int yApp, int size, Point2D.Double elementPosition, double heading) {

        int xCenter = (int) (xApp + size * elementPosition.x);
        int yCenter = (int) (yApp + size * elementPosition.y);
        int radius = (int) (size * 0.10);
        g.setColor(Color.red);
        g.fillOval((int) (xCenter - radius), (int) (yCenter - radius), 2 * radius, 2 * radius);
        g.setColor(Color.black);
        g.drawOval((int) (xCenter - radius), (int) (yCenter - radius), 2 * radius, 2 * radius);
        g.drawString("" + this.headingDegrees + ", " + position, xCenter, yCenter);
    }
}
