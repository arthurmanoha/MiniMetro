package minimetro;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author arthu
 */
public class Wagon extends TrainElement {

    public Wagon(double x, double y, double vx, double vy) {
        super(x, y, vx, vy);
        maxSpeed = 10;
        color = Color.blue;
    }

    public Wagon(double x, double y) {
        this(x, y, 0, 0);
    }

    @Override
    public void paint(Graphics g, int xApp, int yApp, int size) {
        super.paint(g, xApp, yApp, size);
    }
}
