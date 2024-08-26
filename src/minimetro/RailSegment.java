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

    // Distance between the two rails.
    private double railSpacing = 10.0;
    // Length of the wooden tie between the rails
    private double tieLength = 16;

    // Colors of the main components of the rail segment
    private static Color ballastColor = Color.GRAY;
    private static Color railColor = Color.LIGHT_GRAY;
    private static Color tieColor = new Color(0.384f, 0.047f, 0.0f);

    // Coordinates of the ends of the rails and the tie.
    private double length, ux, uy, vx, vy,
            xStartLeft, yStartLeft, xStartRight, yStartRight,
            xEndLeft, yEndLeft, xEndRight, yEndRight,
            xTieLeft, yTieLeft, xTieRight, yTieRight;

    private static int NB_RAILS = 0;
    private int id;

    public RailSegment(double x0, double y0, double x1, double y1) {
        xStart = x0;
        yStart = y0;
        xEnd = x1;
        yEnd = y1;

        // Data used to compute rail and tie coordinates
        length = Math.sqrt((xEnd - xStart) * (xEnd - xStart) + (yEnd - yStart) * (yEnd - yStart));
        ux = (xEnd - xStart) / length;
        uy = (yEnd - yStart) / length;
        vx = -uy;
        vy = ux;

        // Rails coordinates
        xStartLeft = xStart + vx * railSpacing / 2;
        xStartRight = xStart - vx * railSpacing / 2;
        yStartLeft = yStart + vy * railSpacing / 2;
        yStartRight = yStart - vy * railSpacing / 2;
        xEndLeft = xEnd + vx * railSpacing / 2;
        xEndRight = xEnd - vx * railSpacing / 2;
        yEndLeft = yEnd + vy * railSpacing / 2;
        yEndRight = yEnd - vy * railSpacing / 2;

        // Tie coordinates
        xTieLeft = xStart + ux * length / 2 + vx * tieLength / 2;
        xTieRight = xStart + ux * length / 2 - vx * tieLength / 2;
        yTieLeft = yStart + uy * length / 2 + vy * tieLength / 2;
        yTieRight = yStart + uy * length / 2 - vy * tieLength / 2;

        id = NB_RAILS;
        NB_RAILS++;

    }

    void paint(Graphics g, double x0, double y0, double zoom) {

        int panelHeight = g.getClipBounds().height;

        int xStartApp;
        int yStartApp;
        int xEndApp;
        int yEndApp;

        // Rails
        g.setColor(railColor);

        xStartApp = (int) (x0 + xStartLeft * zoom);
        yStartApp = panelHeight - (int) (y0 + yStartLeft * zoom);
        xEndApp = (int) (x0 + xEndLeft * zoom);
        yEndApp = panelHeight - (int) (y0 + yEndLeft * zoom);
        g.drawLine(xStartApp, yStartApp, xEndApp, yEndApp);
        xStartApp = (int) (x0 + xStartRight * zoom);
        yStartApp = panelHeight - (int) (y0 + yStartRight * zoom);
        xEndApp = (int) (x0 + xEndRight * zoom);
        yEndApp = panelHeight - (int) (y0 + yEndRight * zoom);
        g.drawLine(xStartApp, yStartApp, xEndApp, yEndApp);

        // Tie
        int xTieLeftApp = (int) (x0 + xTieLeft * zoom);
        int yTieLeftApp = panelHeight - (int) (y0 + yTieLeft * zoom);
        int xTieRightApp = (int) (x0 + xTieRight * zoom);
        int yTieRightApp = panelHeight - (int) (y0 + yTieRight * zoom);

        g.setColor(tieColor);
        g.drawLine(xTieLeftApp, yTieLeftApp, xTieRightApp, yTieRightApp);
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
        double headingRad = Math.atan2(yEnd - yStart, xEnd - xStart);
        double headingInDegrees = (PI / 2 - headingRad) * 180 / PI;
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

        length = Math.sqrt((xEnd - xStart) * (xEnd - xStart) + (yEnd - yStart) * (yEnd - yStart));
        ux = (xEnd - xStart) / length;
        uy = (yEnd - yStart) / length;

        // A is the start of this segment, B is the end.
        // M denotes the position of the TrainElement.
        // P is the projection of M on AB.
        // Scalar product between vectors AM> and u>
        double am_u = (te.getX() - xStart) * ux + (te.getY() - yStart) * uy;
        double xP = xStart + am_u * ux;
        double yP = yStart + am_u * uy;
        te.setPosition(xP, yP);

        // Align train speed along one of the two possible rail directions.
        double potentialNewHeadingDeg = this.getHeadingInDegrees();

        // Try the forward direction of the rail.
        double deltaHeading = Math.abs(te.getHeadingDeg() - potentialNewHeadingDeg);
        if (deltaHeading > 20 && deltaHeading < 340) {
            // Use the backward rail direction instead.
            potentialNewHeadingDeg = (potentialNewHeadingDeg + 180) % 360;
        }
        te.setHeadingDegrees(potentialNewHeadingDeg);
    }
}
