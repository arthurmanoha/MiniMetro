package minimetro;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.util.ArrayList;

/**
 * This object keeps its previous positions in memory so it may show
 * relativistic behaviors.
 *
 * @author arthu
 */
public abstract class RelativisticElement extends SpriteElement {

    private Point2D.Double absolutePosition;
    protected double linearSpeed;
    private double headingDegrees; // 0:N, 90:E, 180:S, 270:W, MAX_VALUE: not set yet.
    private ArrayList<Point2D.Double> absolutePositionHistory;
    private ArrayList<Double> headingHistory;

    public RelativisticElement(int newId, Point2D.Double newAbsolutePosition) {
        super();

        absolutePosition = newAbsolutePosition;
        absolutePositionHistory = new ArrayList<>();
        absolutePositionHistory.add(absolutePosition);

        headingDegrees = 0;
        headingHistory = new ArrayList<>();
        headingHistory.add(headingDegrees);
    }

    protected double getX() {
        return absolutePosition.x;
    }

    protected double getY() {
        return absolutePosition.y;
    }

    protected Point2D.Double getAbsolutePosition() {
        return absolutePosition;
    }

    protected Point2D.Double getAbsolutePosition(double delay) throws RelativityException {
        try {
            if (absolutePositionHistory == null) {
                return absolutePosition;
            } else if (delay >= 0 && delay < absolutePositionHistory.size()) {

                double decimalPart = delay - (int) delay;
                Point2D.Double positionBefore = absolutePositionHistory.get((int) delay);
                Point2D.Double positionAfter = absolutePositionHistory.get((int) delay + 1);

                double newX = positionBefore.x * (1 - decimalPart) + positionAfter.x * decimalPart;
                double newY = positionBefore.y * (1 - decimalPart) + positionAfter.y * decimalPart;

                return new Point2D.Double(newX, newY);
            }
        } catch (IndexOutOfBoundsException e) {
            // Do nothing here and throw a RelativityException immediately afterwards.
        }
        throw new RelativityException();
    }

    protected void setPosition(Point2D.Double newPos) {

        this.absolutePosition = newPos;
    }

    protected void setPosition(double newX, double newY) {

        setPosition(new Point2D.Double(newX, newY));
    }

    protected void setSpeed(double newVx, double newVy) {
        linearSpeed = sqrt(newVx * newVx + newVy * newVy);
        double newHeadingRad = Math.atan2(newVy, newVx);
        setHeading(radToDeg(newHeadingRad));
    }

    protected void setHeading(double newHeading) {

        this.headingDegrees = newHeading;
    }

    protected double getHeading() {
        return headingDegrees;
    }

    double getHeadingDeg() {
        return getHeading();
    }

    double getHeadingRad() {
        return degToRad(getHeading());
    }

    void setHeadingDegrees(double newHeading) {
        if (newHeading > 360) {
            newHeading -= 360;
        }
        setHeading(newHeading);
    }

    protected double getHeading(double delay) throws RelativityException {
        if (headingHistory == null) {
            return headingDegrees;
        } else if (delay < headingHistory.size()) {

            double decimalPart = delay - (int) delay;
            double headingBefore = headingHistory.get((int) delay);
            double headingAfter = headingHistory.get((int) delay + 1);

            return headingBefore * (1 - decimalPart) + headingAfter * decimalPart;
        } else {
            throw new RelativityException();
        }
    }

    protected double getVx() {
        return linearSpeed * cos(getHeadingRad());
    }

    protected double getVy() {
        return linearSpeed * sin(getHeadingRad());
    }

    protected void move(double dt) {
        Point2D.Double movement = new Point2D.Double(getVx() * dt, getVy() * dt);
        setPosition(getAbsolutePosition().x + movement.x, getAbsolutePosition().y + movement.y);
    }

    protected void updateHistory() {

        // Add the current position and heading to the history.
        absolutePositionHistory.add(0, absolutePosition);
        headingHistory.add(0, headingDegrees);
    }

    protected int getHistorySize() {
        if (absolutePositionHistory != null) {
            return absolutePositionHistory.size();
        } else {
            return -1;
        }
    }

    protected void printHistory() {
        System.out.println("HISTORY{");
        for (int rank = 0; rank < 10; rank++) {
            try {
                System.out.println("    " + rank + ": "
                        + "x = " + absolutePositionHistory.get(rank).x
                        + "y = " + absolutePositionHistory.get(rank).y);
            } catch (NullPointerException | IndexOutOfBoundsException e) {

            }
        }
        System.out.println("}END");
    }

    protected void paint(Graphics g, double x0, double y0, double zoom, double xRef, double yRef, double lightspeed, double currentDt) {
        double delay = findVisibilityDelay(xRef, yRef, lightspeed, currentDt);
        paint(g, x0, y0, zoom, delay);
    }

    /**
     * Find the first instant when the object will be visible from the
     * reference.
     *
     * @param xRef
     * @param yRef
     * @param lightspeed
     * @param dt
     * @return the rank in history of the position currently visible from the
     * reference.
     */
    protected double findVisibilityDelay(double xRef, double yRef, double lightspeed, double dt) {

        boolean foundVisibility = false;

        int rank = 0;
        double prevVisibility = 0;
        try {
            while (rank < absolutePositionHistory.size() && !foundVisibility) {

                Point2D.Double pos = this.absolutePositionHistory.get(rank);
                double dx = pos.x - xRef;
                double dy = pos.y - yRef;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // How long ago the image from that position was emitted (in seconds)
                double ageOfPhoton = rank * dt;
                // How long light takes to reach the reference from that position (in seconds)
                double necessaryTravelTime = distance / lightspeed;
                double visibility = ageOfPhoton - necessaryTravelTime;

                if (visibility > 0 && prevVisibility < 0) {
                    // The object's state at this rank is visible from the reference

                    double result = rank - visibility / (visibility - prevVisibility);
                    return result;
                }

                prevVisibility = visibility;
                rank++;
            }
        } catch (NullPointerException e) {
            // The object is not visible because even the first light it emitted has not reached the ref yet.
            return -1.0;
        }
        // This function should return before this step.
        return -2.0;
    }

    protected double degToRad(double headingDegrees) {
        return ((90 - headingDegrees) * 2 * PI) / 360;
    }

    private double radToDeg(double headingRad) {
        return 90 - headingRad * 360 / (2 * PI);
    }

}
