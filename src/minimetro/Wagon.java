package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.ArrayList;

/**
 *
 * @author arthu
 */
public class Wagon extends TrainElement {

    private ArrayList<Passenger> passengersList;
    private int maxCapacity;
    private int nbPassengerPerRow;

    public Wagon(int newId) {
        super(newId);
        maxSpeed = 10;
        color = Color.blue;
        imagePath = "src\\img\\Wagon.png";
        loadImage();
        passengersList = new ArrayList<>();
        maxCapacity = 6;
        nbPassengerPerRow = 2;
    }

    public Wagon(Point2D.Double newAbsolutePosition) {
        this(-1, newAbsolutePosition);
    }

    public Wagon(int newId, Point2D.Double newAbsolutePosition) {
        this(newId);
        this.absolutePosition = newAbsolutePosition;
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom) {
        super.paint(g, x0, y0, zoom);
        for (Passenger p : passengersList) {
            p.paint(g, x0, y0, zoom);
        }
    }

    @Override
    protected void start() {
    }

    @Override
    protected void stop() {
    }

    private boolean isEven(int n) {
        return 2 * (n / 2) == n;
    }

    @Override
    protected void move(double dt) {
        super.move(dt);
        double rowMargin = 1.05;
        double colMargin = 0.8;
        int nbRows = maxCapacity / nbPassengerPerRow;
        int nbCols = maxCapacity / nbRows;
        int passengerRank = 0;
        for (Passenger p : passengersList) {
            double row = passengerRank / nbPassengerPerRow + (isEven(nbCols) ? 0.0 : 0.5);
            double col = passengerRank % nbPassengerPerRow + (isEven(nbRows) ? 0.0 : 0.5);
            double passengerX = (row - nbRows / 2) * rowMargin;
            double passengerY = (col - nbCols / 2) * colMargin;
            double headingRad = degToRad(headingDegrees);
            double xRotated = this.getX() + passengerX * cos(headingRad) - passengerY * sin(headingRad);
            double yRotated = this.getY() + passengerY * cos(headingRad) + passengerX * sin(headingRad);
            p.setCoordinates(xRotated, yRotated);
            passengerRank++;
        }
    }

    /**
     * Receive a passenger. Only if the wagon is stopped.
     *
     * @param p the boarding passenger
     * @return true if the passenger was accepted, false otherwise
     */
    protected boolean receivePassenger(Passenger p) {
        return receivePassenger(p, false);
    }

    /**
     * Receive a passenger. Only is the wagon is stopped or if we circumvent the
     * speed check (used for adding passengers when loading a file).
     *
     * @param p
     * @param forceReceive
     * @return
     */
    protected boolean receivePassenger(Passenger p, boolean forceReceive) {
        if (isStopped() || forceReceive) {
            if (passengersList.size() < maxCapacity) {
                passengersList.add(p);
                return true;
            }
        }
        return false;
    }

    protected ArrayList<Passenger> dropAllPassengers() {
        ArrayList<Passenger> removedPassengers = new ArrayList<>();
        removedPassengers.addAll(passengersList);
        passengersList.clear();
        return removedPassengers;
    }

    /**
     * Let go of the passangers that have the corresponding target station
     * number.
     *
     * @param stationNumber
     * @return
     */
    protected ArrayList<Passenger> dropPassengers(int stationNumber) {
        ArrayList<Passenger> removedPassengers = new ArrayList<>();
        if (isStopped() && !passengersList.isEmpty()) {

            // Add passengers to the 'getting off now' list.
            for (Passenger p : this.passengersList) {
                if (p.getLastPathStep() == stationNumber || p.getLastPathStep() == Integer.MAX_VALUE) {
                    // This passenger gets off here.
                    removedPassengers.add(p);
                    p.validateFirstPathStep();
                }
            }
            // Remove those passengers from the wagon.
            for (Passenger p : removedPassengers) {
                this.passengersList.remove(p);
            }
        }
        return removedPassengers;
    }

    protected boolean hasRoom() {
        return passengersList.size() < maxCapacity;
    }

    @Override
    public String toString() {
        String text = super.toString();

        for (Passenger p : passengersList) {
            text += "\n" + World.ONBOARD + " " + this.id + " " + p.toString();
        }
        return text;
    }

    protected ArrayList<Passenger> getPassengers() {
        return passengersList;
    }
}
