package minimetro;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import static java.lang.Double.NaN;

/**
 *
 * @author arthu
 */
public class TrainLink {

    private TrainElement e0;
    private TrainElement e1;
    private double defaultLength;
    private double previousLength, currentLength;

    private static int NB_LINKS_CREATED = 0;
    private int id;

    private double strength = 50;
    private double damping = 300;

    public TrainLink(TrainElement elem0, TrainElement elem1, double distance) {
        e0 = elem0;
        e1 = elem1;
        defaultLength = distance;
        previousLength = defaultLength;
        currentLength = defaultLength;
        id = NB_LINKS_CREATED;
        NB_LINKS_CREATED++;
    }

    TrainElement getElement(int i) {
        if (i == 0) {
            return e0;
        } else if (i == 1) {
            return e1;
        }
        return null;
    }

    public String toString() {
        return "Link{" + e0 + ", " + e1 + "}";
    }

    double getDefaultLength() {
        return defaultLength;
    }

    double getStrength() {
        return strength;
    }

    /**
     * Compute the force developed by the link.
     *
     * @param posElem0 absolute position of the first TrainElement
     * @param posElem1 absolute position of the second TrainElement
     * @return
     */
    Point2D.Double computeForce(Point2D.Double posElem0, Point2D.Double posElem1) {

        if (posElem0 != null && posElem1 != null) {
            double dx = posElem1.x - posElem0.x;
            double dy = posElem1.y - posElem0.y;
            currentLength = Math.sqrt(dx * dx + dy * dy);
            double elongation = currentLength - getDefaultLength();

            // unit vector is aligned from cell0 to cell1.
            Point2D.Double unitVector = new Point2D.Double(dx / currentLength, dy / currentLength);
            double stretchForce = computeStretchForce(elongation);
            double dampingForce = -damping * (previousLength - currentLength);
            double force = stretchForce + dampingForce;
            previousLength = currentLength;
            return new Point2D.Double(force * unitVector.x, force * unitVector.y);
        }
        return null;
    }

    private double computeStretchForce(double elongation) {
        return getStrength() * elongation;
    }

    void applyForce(double dt) {
        double dx = e1.absolutePosition.x - e0.absolutePosition.x;
        double dy = e1.absolutePosition.y - e0.absolutePosition.y;

        currentLength = Math.sqrt(dx * dx + dy * dy);
        double elasticForce = (defaultLength - currentLength) * strength;
        double dampingForce = (previousLength - currentLength) * damping;

        double forceIntensity = elasticForce;

        Point2D.Double unitVector = new Point2D.Double(dx / currentLength, dy / currentLength);
        Point2D.Double force = new Point2D.Double(forceIntensity * unitVector.x,
                forceIntensity * unitVector.y);
        Point2D.Double forceOpp = new Point2D.Double(-force.x, -force.y);

        if (force.x != NaN && force.y != NaN) {
            e0.increaseForce(forceOpp);
            e1.increaseForce(force);
        }
    }

    void paint(Graphics g, double x0, double y0, double zoom) {
        Point2D.Double posFirst = e0.absolutePosition;
        Point2D.Double posSecond = e1.absolutePosition;

        int xFirstApp = (int) (x0 + posFirst.x * zoom);
        int yFirstApp = g.getClipBounds().height - (int) (y0 + posFirst.y * zoom);
        int xSecondApp = (int) (x0 + posSecond.x * zoom);
        int ySecondApp = g.getClipBounds().height - (int) (y0 + posSecond.y * zoom);
        g.setColor(Color.black);
        int width = (int) Math.max(1, zoom / 100);
        Stroke s = new BasicStroke(width);
        ((Graphics2D) g).setStroke(s);
        g.drawLine(xFirstApp, yFirstApp, xSecondApp, ySecondApp);
    }

}
