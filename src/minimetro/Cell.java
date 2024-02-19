package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * This is a single 1x1 element of the grid.
 * It may contain one piece of track, one carriage.
 *
 * @author arthu
 */
public class Cell {

    protected Color color;

    private TrainElement trainElement;

    private ArrayList<RailSegment> rails;
    private int nbRails;
    ArrayList<Color> colorList;

    public Cell(ArrayList<RailSegment> oldRails) {
        color = Color.gray;
        trainElement = null;
        if (oldRails == null) {
            rails = new ArrayList<>();
            nbRails = 8;
        } else {
            rails = oldRails;
            nbRails = rails.size();
        }
        colorList = new ArrayList<>();
        colorList.add(Color.red);
        colorList.add(Color.green);
        colorList.add(Color.blue);
        colorList.add(Color.yellow);
        colorList.add(Color.gray);
        colorList.add(Color.orange);
        colorList.add(Color.MAGENTA);
        colorList.add(Color.CYAN);
    }

    public Cell() {
        this(null);
    }

    /**
     * Paint the cell with its background and foreground.
     */
    protected void paint(Graphics g, int row, int col, double x0, double y0, double zoom) {
        final int xApp = (int) (col * zoom + x0);
        final int yApp = (int) (row * zoom + y0);

        // Draw background
        g.setColor(this.color);
        g.fillRect(xApp, yApp, (int) zoom + 1, (int) zoom + 1);

        // Draw borders
        g.setColor(Color.black);
        g.drawLine(xApp, yApp, (int) (xApp + zoom), yApp);
        g.drawLine(xApp, yApp, xApp, (int) (yApp + zoom));

        int i = 0;
        if (rails != null) {
            for (RailSegment railSegment : rails) {
                railSegment.paint(g, xApp, yApp, zoom, colorList.get(i % 6));
                i++;
            }
        }
    }

    /**
     * Set a track section that goes through this cell.
     *
     */
    protected void setTrack(int dxBefore, int dyBefore, int dxAfter, int dyAfter) {
        double xStart = defineBorderCoordinates(dxBefore);
        double xEnd = defineBorderCoordinates(dxAfter);
        double yStart = defineBorderCoordinates(dyBefore);
        double yEnd = defineBorderCoordinates(dyAfter);

        rails.clear();
        // Create rail sections
        for (int i = 0; i < nbRails; i++) {
            double percentageStart = (double) i / nbRails;
            double xSectionStart = xStart * (1 - percentageStart) + xEnd * percentageStart;
            double ySectionStart = yStart * (1 - percentageStart) + yEnd * percentageStart;
            double percentageEnd = ((double) (i + 1)) / nbRails;
            double xSectionEnd = xStart * (1 - percentageEnd) + xEnd * percentageEnd;
            double ySectionEnd = yStart * (1 - percentageEnd) + yEnd * percentageEnd;
            rails.add(new RailSegment(xSectionStart, ySectionStart, xSectionEnd, ySectionEnd));
        }
    }

    /**
     * return a value of 0, 0.5 or 1 depending on where the neighbor cell is
     * located.
     *
     * @param dxy
     * @return
     */
    private double defineBorderCoordinates(double dxy) {
        if (dxy == -1) {
            return 0;
        } else if (dxy == 0) {
            return 0.5;
        } else {
            return 1;
        }
    }

    // A cell has tracks if it is linked to at least one neighbor.
    protected boolean hasRails() {
        return !rails.isEmpty();
    }

    protected void setLoco() {
        if (hasRails()) {
            this.trainElement = new Locomotive();
        }
    }

    protected boolean hasLoco() {
        return this.trainElement != null && (this.trainElement instanceof Locomotive);
    }

    protected Locomotive getLoco() {
        if (hasLoco()) {
            return (Locomotive) this.trainElement;
        } else {
            return null;
        }
    }

    protected void setWagon() {
        if (hasRails()) {
            this.trainElement = new Wagon();
        }
    }

}
