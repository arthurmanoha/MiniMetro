package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

/**
 *
 * @author arthu
 */
public class Wagon extends TrainElement {

    public Wagon() {
    }

    @Override
    public void paint(Graphics g, int xApp, int yApp, int size, Point2D.Double elementPosition, double heading) {
        g.setColor(Color.blue);
        g.fillOval(xApp, yApp, size, size);
        g.setColor(Color.black);
        g.drawOval(xApp, yApp, size, size);
    }
}
