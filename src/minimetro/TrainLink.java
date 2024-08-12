package minimetro;

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

    private double strength = 0.2;
    private double damping = 0.0;

    public TrainLink(TrainElement elem0, TrainElement elem1, double distance) {
        e0 = elem0;
        e1 = elem1;
        defaultLength = distance;
        previousLength = defaultLength;
        currentLength = defaultLength;
        id = NB_LINKS_CREATED;
        NB_LINKS_CREATED++;
        System.out.println("Created new TrainLink " + id + " (" + elem0 + ", " + elem1 + ");");
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
     * @param posC0
     * @param posC1
     * @param posElem0
     * @param posElem1
     * @return
     */
    Point2D.Double computeForce(Point2D.Double posC0, Point2D.Double posC1, Point2D.Double posElem0, Point2D.Double posElem1) {

        if (posC0 != null && posC1 != null && posElem0 != null && posElem1 != null) {
            double dx = (posC1.x - posC0.x) + (posElem1.x - posElem0.x);
            double dy = (posC1.y - posC0.y) + (posElem1.y - posElem0.y);
            previousLength = currentLength;
            currentLength = Math.sqrt(dx * dx + dy * dy);
            double elongation = currentLength - getDefaultLength();
            System.out.println("distance: " + currentLength + ", def length: " + getDefaultLength()
                    + ", elongation: " + elongation);

            // unit vector is aligned from cell0 to cell1.
            Point2D.Double unitVector = new Point2D.Double(dx / currentLength, dy / currentLength);

            System.out.println("Applying force in direction (" + dx + ", " + dy + ");");
            double force = elongation * getStrength() - damping * (previousLength - currentLength);
            return new Point2D.Double(force * unitVector.x, force * unitVector.y);
        }
        return null;
    }

    void applyForce(double dt) {
        double dx = e1.absolutePosition.x - e0.absolutePosition.x;
        double dy = e1.absolutePosition.y - e0.absolutePosition.y;

        currentLength = Math.sqrt(dx * dx + dy * dy);
        System.out.println("current length: " + currentLength);
        double elasticForce = (defaultLength - currentLength) * strength;
        double dampingForce = (previousLength - currentLength) * damping;

        System.out.println("elastic force: " + elasticForce);
        System.out.println("damping force: " + dampingForce);

        double forceIntensity = elasticForce;

        Point2D.Double unitVector = new Point2D.Double(dx / currentLength, dy / currentLength);
        Point2D.Double force = new Point2D.Double(forceIntensity * unitVector.x,
                forceIntensity * unitVector.y);
        Point2D.Double forceOpp = new Point2D.Double(-force.x, -force.y);

        System.out.println("final force: " + force);
        if (force.x != NaN && force.y != NaN) {
            System.out.println("    increasing force for both elements");
            e0.increaseForce(forceOpp);
            e1.increaseForce(force);
        }
    }

}
