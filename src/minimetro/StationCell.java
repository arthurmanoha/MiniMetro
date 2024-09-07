package minimetro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import static java.lang.Math.max;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

    // Directions of the connected stations.
    private HashMap<CardinalPoint, StationCell> walkways;

    public StationCell(Cell previousCell) {
        super(previousCell);
        this.color = Color.yellow;
        id = NB_STATIONS_CREATED;
        NB_STATIONS_CREATED++;
        passengerList = new ArrayList<>();
        walkways = new HashMap<>();

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

    public static void resetNbStationsCreated() {
        NB_STATIONS_CREATED = 0;
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

        if (passenger.getX() == Double.MAX_VALUE) {
            double newX = this.absolutePosition.x + (new Random().nextDouble() - 0.5) * cellSize / 2;
            double newY = this.absolutePosition.y + (new Random().nextDouble() - 0.5) * cellSize / 2;
            passenger.setCoordinates(newX, newY);
        }
        passenger.stopWalking();

        this.passengerList.add(passenger);
    }

    private boolean hasPassengers() {
        return passengerList != null && !passengerList.isEmpty();
    }

    @Override
    protected void getPassengersOff() {
        // Find a stopped Wagon
        for (TrainElement te : trainElements) {
            if (te instanceof Wagon) {
                if (te.isStopped()) {
                    Wagon wagon = (Wagon) te;
                    this.passengerList.addAll(wagon.dropPassengers(this.id));
                }
            }
        }

        for (Passenger p : passengerList) {
            World.map.computePath(this.getId(), p);
        }
    }

    @Override
    protected void boardPassengers() {

        WorldMap map = World.map;

        Iterator<Passenger> iter = passengerList.iterator();
        while (iter.hasNext()) {
            Passenger p = iter.next();
            map.computePath(this.getId(), p);

            int currentTargetId = p.getLastPathStep();
            TrainLine line = map.findCurrentLine(this.getId());
            if (line != null && line.containsStation(currentTargetId)) {
                // This is the appropriate line for this passenger.
                Wagon w = this.findStoppedWagonWithRoom();
                if (w != null) {
                    // Passenger gets on board.
                    w.receivePassenger(p);
                    iter.remove();
                }
            }
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

    /**
     * Move passengers following these rules, in a descending order of priority:
     * passengers on a connection shall walk to the nearby station,
     * passengers shall move away from each other,
     * passengers shall stay away from the tracks and the limits of the cell.
     *
     * @param dt
     */
    @Override
    protected void movePassengers(double dt) {

        Iterator<Passenger> iter = passengerList.iterator();
        while (iter.hasNext()) {
            Passenger p = iter.next();
            int otherStationId = p.getLastPathStep();

            StationCell neighborCell = null;
            for (CardinalPoint direction : walkways.keySet()) {
                neighborCell = walkways.get(direction);
                if (neighborCell != null && neighborCell.getId() == otherStationId) {
                    // Passenger wants to go to neighborCell.
                    switch (direction) {
                    case NORTH:
                        p.setSpeed(0, 1);
                        break;
                    case EAST:
                        p.setSpeed(1, 0);
                        break;
                    case SOUTH:
                        p.setSpeed(0, -1);
                        break;
                    case WEST:
                        p.setSpeed(-1, 0);
                        break;
                    default:
                        p.setSpeed(0, 0);
                    }
                }
            }
            p.move(dt);
            if (hasLeftCell(p) && neighborCell != null) {
                iter.remove();
                neighborCell.addPassenger(p);
            }
        }
    }

    private boolean hasLeftCell(Passenger p) {
        return (p.getX() > this.absolutePosition.x + cellSize / 2
                || p.getX() < this.absolutePosition.x - cellSize / 2
                || p.getY() > this.absolutePosition.y + cellSize / 2
                || p.getY() < this.absolutePosition.y - cellSize / 2);
    }

    @Override
    protected void addWalkwayDirection(StationCell connectedStation, CardinalPoint cardinalPoint
    ) {
        this.walkways.put(cardinalPoint, connectedStation);
    }

    private Wagon findStoppedWagonWithRoom() {
        for (TrainElement te : trainElements) {
            if (te instanceof Wagon) {
                Wagon w = (Wagon) te;
                if (w.hasRoom() && w.isStopped()) {
                    return w;
                }
            }
        }
        return null;
    }
}
