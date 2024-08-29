package minimetro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import static java.lang.Math.max;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author arthu
 */
public class StationCell extends Cell {

    private static int NB_STATIONS_CREATED = 0;
    private int id;
    ArrayList<Passenger> passengerList;

    public StationCell(Cell previousCell) {
        super(previousCell);
        this.color = Color.yellow;
        id = NB_STATIONS_CREATED;
        NB_STATIONS_CREATED++;
        passengerList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    @Override
    protected void paint(Graphics g, double x0, double y0, double zoom) {
        super.paint(g, x0, y0, zoom);

        // On-screen coordinates of the center of the cell:
        final double xApp = absolutePosition.x * zoom + x0;
        final double yApp = g.getClipBounds().height - (absolutePosition.y * zoom + y0);
        final double appSize = zoom * cellSize;

        g.setColor(Color.black);
        g.setFont(new Font("helvetica", Font.PLAIN, (int) (max(10, appSize / 15))));
        int textHeight = g.getFontMetrics().getHeight();
        g.drawString(id + "",
                (int) (xApp - appSize / 2 + 1),
                (int) (yApp - appSize / 2 + textHeight));
        for (Passenger p : passengerList) {
            p.paint(g, x0, y0, zoom);
        }
    }

    protected void addPassenger(Passenger passenger) {

        double newX = this.absolutePosition.x + (new Random().nextDouble() - 0.5) * cellSize / 2;
        double newY = this.absolutePosition.y + (new Random().nextDouble() - 0.5) * cellSize / 2;
        passenger.setCoordinates(newX, newY);

        this.passengerList.add(passenger);
    }

    private boolean hasPassengers() {
        return passengerList != null && !passengerList.isEmpty();
    }

    @Override
    protected void getPassengersOff() {
        // Find a stopped Wagon
        for (TrainElement te : trainElements) {
            if (te instanceof Wagon && te.isStopped()) {
                Wagon w = (Wagon) te;
                this.passengerList.addAll(w.dropAllPassengers());
            }
        }
    }

    @Override
    protected void boardPassengers() {
        if (hasPassengers()) {
            // Find a stopped Wagon
            for (TrainElement te : trainElements) {
                if (te instanceof Wagon && te.isStopped()) {
                    Wagon w = (Wagon) te;
                    for (Passenger p : passengerList) {
                        w.receivePassenger(p);
                    }
                    passengerList.clear();
                }
            }
        }
    }
}
