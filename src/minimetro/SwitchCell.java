package minimetro;

import java.awt.Graphics;

/**
 *
 * A SwitchCell is a Cell with two sets of railways.
 * It is made with two distinct Cells with tracks that have one point in common.
 *
 * @author arthu
 */
public class SwitchCell extends Cell {

    private Cell cellA, cellB;

    // Default: cellA is active; when toggled: cellB is active.
    private boolean toggled;

    private static int NB_SWITCHES_CREATED = 0;
    private int id;

    // First entry: shared neighbor
    // Second and third: independent neighbors
    private CardinalPoint connections[];

    public SwitchCell() {
        this(null);
        id = NB_SWITCHES_CREATED;
        NB_SWITCHES_CREATED++;
    }

    public SwitchCell(Cell c) {
        super(c);
        cellA = c;
        cellB = new Cell(cellA);
        connections = new CardinalPoint[3];
        connections[0] = null;
        connections[1] = null;
        connections[2] = null;
    }

    @Override
    protected void paintForeground(Graphics g, double x0, double y0, double zoom) {
        if (toggled) {
            // B is active and painted last
            cellA.paintForeground(g, x0, y0, zoom);
            cellB.paintForeground(g, x0, y0, zoom);
        } else {
            // A is active and painted last
            cellB.paintForeground(g, x0, y0, zoom);
            cellA.paintForeground(g, x0, y0, zoom);
        }
        final double xApp = absolutePosition.x * zoom + x0;
        final double yApp = g.getClipBounds().height - (absolutePosition.y * zoom + y0);
        final double appSize = zoom * cellSize;
        String text = "Switch ";
        for (CardinalPoint cp : connections) {
            text += cp + " ";
        }
        g.drawString(text, (int) (xApp - appSize / 2 + 5), (int) (yApp - appSize / 2 + 30));
    }

    /**
     * Add a new direction at the end.
     * The second direction becomes the shared first one, third becomes second.
     *
     * @param x
     * @param y
     */
    void setSwitchPoint(double x, double y) {
        CardinalPoint cp = computeDirection(x, y);

        connections[0] = connections[1];
        connections[1] = connections[2];
        connections[2] = cp;

        if (connections[0] != null) {
            cellA.removeTracksAndLinks();
            cellB.removeTracksAndLinks();
            // First entry: shared neighbor
            // Second and third: independent neighbors
            cellA.addLink(connections[0]);
            cellA.addLink(connections[1]);
            cellB.addLink(connections[0]);
            cellB.addLink(connections[2]);
        }
    }

    void resetConnections() {
        cellA.removeTracksAndLinks();
        cellB.removeTracksAndLinks();
    }

    private CardinalPoint computeDirection(double x, double y) {
        if (x < 0.33) {
            // W
            if (y < 0.33) {
                // NW
                return CardinalPoint.NORTHWEST;
            } else if (y > 0.67) {
                // SW
                return CardinalPoint.SOUTHWEST;
            } else {
                // W
                return CardinalPoint.WEST;
            }
        } else if (x > 0.67) {
            // E
            if (y < 0.33) {
                // NE
                return CardinalPoint.NORTHEAST;
            } else if (y > 0.67) {
                // SE
                return CardinalPoint.SOUTHEAST;
            } else {
                // E
                return CardinalPoint.EAST;
            }
        } else {
            if (y < 0.33) {
                // N
                return CardinalPoint.NORTH;
            } else if (y > 0.67) {
                // S
                return CardinalPoint.SOUTH;
            } else {
                // C
                return CardinalPoint.CENTER;
            }
        }
    }

    @Override
    protected String getLinks() {
        return "S " + cellA.getLinks() + " / " + cellB.getLinks();
    }
}
