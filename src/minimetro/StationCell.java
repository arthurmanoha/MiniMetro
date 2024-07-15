package minimetro;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author arthu
 */
public class StationCell extends Cell {

    public StationCell(double newX, double newY, double newWidth) {
        super(newX, newY, newWidth);
        this.color = Color.black;
    }

    @Override
    protected void paint(Graphics g, double x0, double y0, double zoom) {
        super.paint(g, x0, y0, zoom);
    }
}
