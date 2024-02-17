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

    public Cell() {

    }

    void paint(Graphics g, int row, int col) {
        int size = 40;
        g.setColor(Color.gray);
        g.fillRect(col * size, row * size, size, size);
        g.setColor(Color.black);
        g.drawRect(col * size, row * size, size, size);
        g.drawString(row + ", " + col, (int) ((col + 0.1) * size), (int) ((row + 0.5) * size));
    }
}
