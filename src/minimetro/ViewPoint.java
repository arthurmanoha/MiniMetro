package minimetro;

/**
 * This class defines a point of view on the world, i.e. a pan and zoom.
 *
 * @author arthu
 */
public class ViewPoint {

    private double x0, y0, zoom;

    public ViewPoint(String text) {
        String[] tab = text.split(" ");
        x0 = Double.valueOf(tab[0]);
        y0 = Double.valueOf(tab[1]);
        zoom = Double.valueOf(tab[2]);
    }

    public double getX() {
        return x0;
    }

    public double getY() {
        return y0;
    }

    public double getZoom() {
        return zoom;
    }
}
