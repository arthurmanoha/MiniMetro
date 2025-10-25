package minimetro;

import java.awt.Graphics;
import java.awt.geom.Point2D;

/**
 *
 * @author arthu
 */
public class Reference extends TrainElement {

    public Reference(Point2D.Double newAbsolutePosition) {
        super(0);
        this.absolutePosition = newAbsolutePosition;
        this.headingDegrees = 0;
        spriteZoomLevel *= 2;
        imagePath = "src\\menuIcons\\relativityReference.png";
        loadImage();
    }

    @Override
    protected void start() {
    }

    @Override
    protected void stop() {
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom) {
        super.paint(g, x0, y0, zoom);
    }

    @Override
    protected Point2D.Double getAbsolutePosition(int delay) throws RelativityException {
        return this.absolutePosition;
    }

    @Override
    protected double getHeading(int delay) throws RelativityException {
        return headingDegrees;
    }

    @Override
    protected void setPosition(double newX, double newY) {
        super.setPosition(newX, newY);
    }
}
