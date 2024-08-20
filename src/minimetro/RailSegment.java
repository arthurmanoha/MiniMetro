package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import static java.lang.Math.PI;

/**
 * This is one part of the rails in one given cell.
 *
 * @author arthu
 */
public class RailSegment {

    // Absolute coordinates of the start and end of this segment.
    private final double xStart, yStart, xEnd, yEnd;

    public RailSegment(double x0, double y0, double x1, double y1) {
        xStart = x0;
        yStart = y0;
        xEnd = x1;
        yEnd = y1;
    }

    void paint(Graphics g, double x0, double y0, double zoom, Color c) {

        int panelHeight = g.getClipBounds().height;

        int xStartApp = (int) (x0 + xStart * zoom);
        int yStartApp = panelHeight - (int) (y0 + yStart * zoom);
        int xEndApp = (int) (x0 + xEnd * zoom);
        int yEndApp = panelHeight - (int) (y0 + yEnd * zoom);

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

    /**
     * Get the distance from this rail segment to a givent TrainElement
     *
     * @param te
     * @return the distance from the start of this rail to the TrainElement, or
     * the distance from the end of this rail to the TrainElement, whichever is
     * smaller.
     */
    double getDistance(TrainElement te) {
        double dxStart = te.getX() - this.xStart;
        double dyStart = te.getY() - this.yStart;
        double dxEnd = te.getX() - this.xEnd;
        double dyEnd = te.getY() - this.yEnd;
        double dStart = Math.sqrt(dxStart * dxStart + dyStart * dyStart);
        double dEnd = Math.sqrt(dxEnd * dxEnd + dyEnd * dyEnd);
        return Math.min(dStart, dEnd);
    }

    /**
     * Project the TE ont this rail.
     *
     * @param te
     */
    void snapTrain(TrainElement te) {

        double length = Math.sqrt((xEnd - xStart) * (xEnd - xStart) + (yEnd - yStart) * (yEnd - yStart));
        double ux = (xEnd - xStart) / length;
        double uy = (yEnd - yStart) / length;

        // A is the start of this segment, B is the end.
        // M denotes the position of the TrainElement.
        // P is the projection of M on AB.
        // Scalar product between vectors AM> and u>
        double am_u = (te.getX() - xStart) * ux + (te.getY() - yStart) * uy;
        double xP = xStart + am_u * ux;
        double yP = yStart + am_u * uy;
        te.setPosition(xP, yP);

        // Align train speed along rail direction
        // u_v is the scalar product between unit vector u> and current speed.
        double u_v = ux * te.currentSpeed.x + uy * te.currentSpeed.y;
        te.currentSpeed.x = u_v * ux;
        te.currentSpeed.y = u_v * uy;

        // HeadingRad is 0 for east, pi/2 for north.
        // HeadingDeg is 90 for east, 0 for north.
        double headingRad;
        if (te.isStopped()) {
            headingRad = Math.atan2(uy, ux);
        } else {
            headingRad = Math.atan2(te.currentSpeed.y, te.currentSpeed.x);
        }

        // HedingDeg is 0 for north, 90 for east.
        double headingDeg = (Math.PI / 2 - headingRad) * 360 / (2 * PI);
        te.setHeadingDegrees(headingDeg);
    }
}
