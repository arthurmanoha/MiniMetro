package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import static java.lang.Math.PI;
import java.util.ArrayList;

/**
 * This is one part of the rails in one given cell.
 *
 * @author arthu
 */
public class RailSegment {

    private double xStart, yStart, xEnd, yEnd;

    public RailSegment(double x0, double y0, double x1, double y1) {
        xStart = x0;
        yStart = y0;
        xEnd = x1;
        yEnd = y1;
    }

    void paint(Graphics g, int xApp, int yApp, double zoom, Color c) {

        int xStartApp = (int) (xApp + xStart * zoom);
        int yStartApp = (int) (yApp + yStart * zoom);
        int xEndApp = (int) (xApp + xEnd * zoom);
        int yEndApp = (int) (yApp + yEnd * zoom);

        g.setColor(c);
        g.drawLine(xStartApp, yStartApp, xEndApp, yEndApp);
    }

    public double getXStart() {
        return xStart;
    }

    public double getYStart() {
        return yStart;
    }

    public double getXEnd() {
        return xEnd;
    }

    public double getYEnd() {
        return yEnd;
    }

    /**
     * Compute the rail segment heading in degrees East from North
     * 0 <-> North
     * 90<-> East
     */
    double getHeadingInDegrees() {
        // N:0, E:90, S:180, W:270
        double hNorthToEast = Math.atan2(yEnd - yStart, xEnd - xStart) + PI / 2;
        double headingInDegrees = hNorthToEast * 360 / (2 * Math.PI);
        if (headingInDegrees < 0) {
            headingInDegrees += 360;
        }
        return headingInDegrees;
    }
}
