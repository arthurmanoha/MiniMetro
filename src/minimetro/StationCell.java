package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 *
 * @author arthu
 */
public class StationCell extends Cell {

    private static int NB_STATIONS_CREATED = 0;
    private int id;

    public StationCell(Cell previousCell) {
        super(previousCell);
        this.color = Color.yellow;
        id = NB_STATIONS_CREATED;
        NB_STATIONS_CREATED++;
    }

    @Override
    protected void paint(Graphics g, double x0, double y0, double zoom) {
        super.paint(g, x0, y0, zoom);

        // On-screen coordinates of the center of the cell:
        final double xApp = absolutePosition.x * zoom + x0;
        final double yApp = g.getClipBounds().height - (absolutePosition.y * zoom + y0);
        final double appSize = zoom * cellSize;

        g.setColor(Color.black);
        int textHeight = g.getFontMetrics().getHeight();
        g.drawString(id + "",
                (int) (xApp - appSize / 2 + 1),
                (int) (yApp - appSize / 2 + textHeight));
    }
}
