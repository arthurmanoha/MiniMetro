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
    private static ArrayList<Color> colorList;
    private int id;
    protected ArrayList<Passenger> passengerList;

    public StationCell(Cell previousCell) {
        super(previousCell);
        this.color = Color.yellow;
        id = NB_STATIONS_CREATED;
        NB_STATIONS_CREATED++;
        passengerList = new ArrayList<>();

        colorList = new ArrayList<>();
        colorList.add(Color.red.darker().darker());
        colorList.add(Color.red);
        colorList.add(Color.green);
        colorList.add(Color.green.darker().darker());
        colorList.add(Color.blue.darker().darker());
        colorList.add(Color.blue);
        colorList.add(Color.CYAN);
        colorList.add(Color.yellow);
        colorList.add(Color.orange);
        colorList.add(Color.MAGENTA);
        colorList.add(Color.gray.darker());
        int colorIndex = id % colorList.size();
        color = colorList.get(colorIndex);
    }

    public int getId() {
        return id;
    }

    protected static Color getStationColor(int stationNumber) {
        return colorList.get(stationNumber % colorList.size());
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
                Wagon wagon = (Wagon) te;
                this.passengerList.addAll(wagon.dropPassengers(this.id));
            }
        }
    }

    @Override
    protected void boardPassengers() {
        ArrayList<Passenger> boardingPassengers = new ArrayList<>();

        if (hasPassengers()) {
            for (Passenger p : passengerList) {
                if (p.getTargetStationId() != this.id) {

                    // Try to fit the current passenger into the first non-full wagon.
                    boolean passengerStillOnPlatform = true;
                    for (TrainElement te : trainElements) {
                        if (passengerStillOnPlatform) {
                            if (te instanceof Wagon && te.isStopped()) {
                                Wagon wagon = (Wagon) te;
                                if (wagon.hasRoom()) {
                                    wagon.receivePassenger(p);
                                    passengerStillOnPlatform = false;
                                    boardingPassengers.add(p);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Passenger p : boardingPassengers) {
            passengerList.remove(p);
        }
    }

    /**
     * This function is used to tell a TrainElement that it now is in a station.
     *
     * @param newTrain
     * @return
     */
    @Override
    protected TrainElement addTrainElement(TrainElement newTrain) {
        super.addTrainElement(newTrain);
        if (newTrain instanceof Locomotive) {

            Locomotive newLoco = (Locomotive) newTrain;
            newLoco.addStationToLine(this);
        }
        return null;
    }
}
