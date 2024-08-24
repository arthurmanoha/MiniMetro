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
        maxSpeed = 10;
        color = Color.blue;
        imagePath = "src\\img\\Wagon.png";
        loadImage();
    }

    public Wagon(Point2D.Double newAbsolutePosition) {
        this();
        this.absolutePosition = newAbsolutePosition;
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom) {
        super.paint(g, x0, y0, zoom);
    }

    @Override
    protected void start() {
    }

    @Override
    protected void stop() {
    }
}
