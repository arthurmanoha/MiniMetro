package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 *
 * @author arthu
 */
public class StationCell extends Cell {

    /**
     * Main constructor, with list of rails
     *
     * @param oldRails rails used by another cell (located at the same spot)
     */
    public StationCell(ArrayList<RailSegment> oldRails) {
        super(oldRails);
        this.color = Color.black;
    }

    /**
     * Default constructor for a new cell.
     *
     */
    public StationCell() {
        this(null);
    }

    @Override
    protected void paint(Graphics g, int row, int col, int x0, int y0, double zoom) {
        super.paint(g, row, col, x0, y0, zoom);
    }
}
