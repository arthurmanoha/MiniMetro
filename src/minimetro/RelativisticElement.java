package minimetro;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * This object keeps its previous positions in memory so it may show
 * relativistic behaviors.
 *
 * @author arthu
 */
public abstract class RelativisticElement extends SpriteElement {

    private Point2D.Double absolutePosition;
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

    protected Point2D.Double getAbsolutePosition(int delay) throws RelativityException {
        if (absolutePositionHistory == null) {
            return absolutePosition;
        } else if (delay >= 0 && delay < absolutePositionHistory.size()) {
            return absolutePositionHistory.get(delay);
        } else {
            throw new RelativityException();
        }
    }

    protected void setPosition(Point2D.Double newPos) {
        this.absolutePosition = newPos;
    }

    protected void setPosition(double newX, double newY) {

        setPosition(new Point2D.Double(newX, newY));
    }

    protected void setHeading(double newHeading) {

        this.headingDegrees = newHeading;
    }

    protected double getHeading() {
        return headingDegrees;
    }

    protected double getHeading(int delay) throws RelativityException {
        if (headingHistory == null) {
            return headingDegrees;
        } else if (delay < headingHistory.size()) {
            return headingHistory.get(delay);
        } else {
            throw new RelativityException();
        }
    }

    protected void move(double dt) {

        // Add the current position and heading to the history.
        absolutePositionHistory.add(0, absolutePosition);
        headingHistory.add(0, headingDegrees);
    }

    protected void paint(Graphics g, double x0, double y0, double zoom, double xRef, double yRef) {
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
    protected int findVisibilityDelay(double xRef, double yRef, double lightspeed, double dt) {

        boolean foundVisibility = false;

        int rank = 0;
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
                if (ageOfPhoton > necessaryTravelTime) {
                    // The object's state at this rank is visible from the reference
                    return rank;
                }

                rank++;
            }
        } catch (NullPointerException e) {
            // The object is not visible because even the first light it emitted has not reached the ref yet.
            return -1;
        }
        // This function should return before this step.
        return -2;
    }
}
