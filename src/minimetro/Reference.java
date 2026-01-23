package minimetro;

import java.awt.Graphics;
import java.awt.geom.Point2D;

/**
 *
 * @author arthu
 */
public class Reference extends TrainElement {

    private boolean isActive;

    public Reference(Point2D.Double newAbsolutePosition) {
        super(0, null);
        this.setPosition(newAbsolutePosition);
        this.setHeading(0);
        spriteZoomLevel *= 2;
        imagePath = "src\\menuIcons\\relativityReference_inactive.png";
        loadImage();
        isActive = false;
    }

    @Override
    protected void start() {
        isActive = true;
        updateImage();
    }

    @Override
    protected void stop() {
        isActive = false;
        updateImage();
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom) {
        super.paint(g, x0, y0, zoom);
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom, double delay) {
        super.paint(g, x0, y0, zoom, delay);
    }

    @Override
    protected Point2D.Double getAbsolutePosition(double delay) throws RelativityException {
        return this.getAbsolutePosition();
    }

    @Override
    protected double getHeading(double delay) throws RelativityException {
        return getHeading();
    }

    @Override
    protected void setPosition(double newX, double newY) {
        super.setPosition(newX, newY);
    }

    protected void toggleRelativity() {
        isActive = !isActive;
        updateImage();
    }

    private void updateImage() {
        if (isActive) {
            imagePath = "src\\menuIcons\\relativityReference_active.png";
        } else {
            imagePath = "src\\menuIcons\\relativityReference_inactive.png";
        }
        loadImage();
    }

    public boolean isActive() {
        return this.isActive;
    }
}
