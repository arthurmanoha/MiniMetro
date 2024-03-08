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
        super();
        position = 0.5;
        currentSpeed = 0;
        maxSpeed = 10;
        color = Color.blue;
    }

    @Override
    public void paint(Graphics g, int xApp, int yApp, int size, Point2D.Double elementPosition, double heading) {
        System.out.println("Wagon paint");
        super.paint(g, xApp, yApp, size, elementPosition, heading);
    }
}
