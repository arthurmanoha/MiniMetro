package minimetro;

import java.awt.Color;
import java.awt.Graphics;

/**
 * This is a single 1x1 element of the grid.
 * It may contain one piece of track, one carriage.
 *
 * @author arthu
 */
public class Cell {

    Color color;

    public Cell() {
        color = Color.gray;
    }

    void paint(Graphics g, int row, int col, int x0, int y0, double zoom) {
        int size = (int) zoom;
        g.setColor(this.color);
        g.fillRect(col * size + x0, row * size + y0, size, size);
        g.setColor(Color.black);
        g.drawRect(col * size + x0, row * size + y0, size, size);
    }
}
