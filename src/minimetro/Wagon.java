package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author arthu
 */
public class Wagon extends TrainElement {

    private ArrayList<Passenger> passengersList;

    public Wagon() {
        super();
        maxSpeed = 10;
        color = Color.blue;
        imagePath = "src\\img\\Wagon.png";
        loadImage();
        passengersList = new ArrayList<>();
    }

    public Wagon(Point2D.Double newAbsolutePosition) {
        this();
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

    @Override
    protected void move(double dt) {
        super.move(dt);
        double margin = 10;
        int passengerRank = 0;
        for (Passenger p : passengersList) {
            p.setCoordinates(this.getX(), this.getY() + margin * passengerRank);
            passengerRank++;
        }
    }

    protected void receivePassenger(Passenger p) {
        passengersList.add(p);
    }

    protected void dropPassenger(Passenger p) {
        passengersList.remove(p);
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

        // Add passengers to the 'getting off now' list.
        for (Passenger p : this.passengersList) {
            if (p.getTargetStationId() == stationNumber) {
                // This passenger gets off here.
                removedPassengers.add(p);
            }
        }
        // Remove those passengers from the wagon.
        for (Passenger p : removedPassengers) {
            this.passengersList.remove(p);
        }
        return removedPassengers;
    }
}
