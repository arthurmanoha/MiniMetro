package minimetro;

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
    private ArrayList<Point2D.Double> absolutionPositionHistory;
    private ArrayList<Double> headingHistory;

    public RelativisticElement(int newId, Point2D.Double newAbsolutePosition) {
        super();

        absolutePosition = newAbsolutePosition;
        absolutionPositionHistory = new ArrayList<>();
        absolutionPositionHistory.add(absolutePosition);

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
        if (absolutionPositionHistory == null) {
            return absolutePosition;
        } else if (delay < absolutionPositionHistory.size()) {
            return absolutionPositionHistory.get(delay);
        } else {
            throw new RelativityException();
        }
    }

    protected void setPosition(Point2D.Double newPos) {
        this.absolutePosition = newPos;
        absolutionPositionHistory.add(0, newPos);
    }

    protected void setPosition(double newX, double newY) {

        setPosition(new Point2D.Double(newX, newY));
    }

    protected void setHeading(double newHeading) {

        this.headingDegrees = newHeading;
        this.headingHistory.add(0, newHeading);
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
        absolutionPositionHistory.add(0, absolutePosition);
        headingHistory.add(0, headingDegrees);
    }
}
