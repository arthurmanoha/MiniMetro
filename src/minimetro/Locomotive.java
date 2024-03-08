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
        super();
        position = 0.5;
        currentSpeed = 1;
        maxSpeed = 1;
        color = Color.red;
    }

    @Override
    public void paint(Graphics g, int xApp, int yApp, int size, Point2D.Double elementPosition, double heading) {
        super.paint(g, xApp, yApp, size, elementPosition, heading);
    }
}
