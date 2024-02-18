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
    // Keep track of the neighbor cells this cell is linked to by railroad.
    boolean isLinkedN;
    boolean isLinkedE;
    boolean isLinkedS;
    boolean isLinkedW;

    public Cell() {
        color = Color.gray;
        isLinkedN = false;
        isLinkedE = false;
        isLinkedS = false;
        isLinkedW = false;
    }

    /**
     * Paint the cell with its background and foreground.
     */
    void paint(Graphics g, int row, int col, int x0, int y0, double zoom) {
        int size = (int) zoom;
        g.setColor(this.color);
        g.fillRect(col * size + x0, row * size + y0, size, size);
        g.setColor(Color.black);
        g.drawRect(col * size + x0, row * size + y0, size, size);

        int trackWidth = (int) (zoom / 4);
        g.setColor(Color.black);
        if (isLinkedN) {
            g.fillRect((int) ((col + 0.5) * size - trackWidth / 2 + x0),
                    row * size + y0,
                    trackWidth,
                    size / 2);
        }
        if (isLinkedS) {
            g.fillRect((int) ((col + 0.5) * size - trackWidth / 2 + x0),
                    (int) ((row + 0.5) * size + y0),
                    trackWidth,
                    size / 2);
        }
        if (isLinkedE) {
            g.fillRect((int) ((col + 0.5) * size + x0),
                    (int) ((row + 0.5) * size - trackWidth / 2 + y0),
                    size / 2,
                    trackWidth);
        }
        if (isLinkedW) {
            g.fillRect((col * size + x0),
                    (int) ((row + 0.5) * size - trackWidth / 2 + y0),
                    size / 2,
                    trackWidth);
        }
    }

    /**
     * Set a track section between this cell and one neighbor.
     *
     * @param dRow row number difference between this cell and the neighbor
     * @param dCol col number difference between this cell and the neighbor
     */
    void setTrack(int dRow, int dCol) {
        if (dRow == -1 && dCol == 0) {
            isLinkedN = true;
        } else if (dRow == +1 && dCol == 0) {
            isLinkedS = true;
        } else if (dRow == 0 && dCol == -1) {
            isLinkedW = true;
        } else if (dRow == 0 && dCol == +1) {
            isLinkedE = true;
        }
    }
}
