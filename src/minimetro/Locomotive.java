package minimetro;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author arthu
 */
public class Locomotive extends TrainElement {

    public Locomotive(double x, double y, double newVx, double newVy) {
        super(x, y, newVx, newVy);
        maxSpeed = 1;
        color = Color.red;
    }

    public Locomotive(double x, double y) {
        this(x, y, 0, 0);
    }

    @Override
    public void paint(Graphics g, int xApp, int yApp, int size) {
        super.paint(g, xApp, yApp, size);
    }

    @Override
    public String toString() {
        return "L{" + x + ", " + y + "}";
    }
}
