package minimetro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.FileWriter;
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
    private double LATERAL_MARGIN_PERCENTAGE = 25;

    private static ArrayList<Color> colorList;
    protected int stationId;
    protected ArrayList<Passenger> passengerList;
    protected ArrayList<Passenger> passengersLeavingCell;

    // Directions of the connected stations.
    private HashMap<CardinalPoint, StationCell> walkways;

    /**
     *
     * @param previousCell the cell we replace, so that we know the tracks, row,
     * col, ...
     * @param newId specify an iD or set -1 to let the program choose the first
     * available number
     */
    public StationCell(Cell previousCell, int newId) {
        super(previousCell);
        this.color = Color.yellow;
        if (newId == -1) {
            stationId = NB_STATIONS_CREATED;
            NB_STATIONS_CREATED++;
            System.out.println("Created station " + stationId + " in sequence, NB_STATIONS_CREATED = " + NB_STATIONS_CREATED);
        } else {
            stationId = newId;
            NB_STATIONS_CREATED = max(NB_STATIONS_CREATED, newId + 1);
            System.out.println("Created station with specified id " + stationId + ", NB_STATIONS_CREATED = " + NB_STATIONS_CREATED);
        }
        this.row = previousCell.row;
        this.col = previousCell.col;
        passengerList = new ArrayList<>();
        passengersLeavingCell = new ArrayList<>();
        walkways = new HashMap<>();
        color = getStationColor(stationId);
        this.stopTimerDuration = 2.0;
    }

    public StationCell(Cell previousCell) {
        this(previousCell, -1);
    }

    public int getId() {
        return stationId;
    }

    public static void resetNbStationsCreated() {
        NB_STATIONS_CREATED = 0;
    }

    protected static Color getStationColor(int stationNumber) {
        if (stationNumber == -1) {
            return Color.white;
        } else if (colorList == null) {

            colorList = new ArrayList<>();
            colorList.add(Color.red.darker().darker());
            colorList.add(Color.red);
            colorList.add(Color.green);
            colorList.add(Color.green.darker().darker());
            colorList.add(Color.blue.darker().darker());
            colorList.add(Color.blue);
            colorList.add(Color.yellow);
            colorList.add(Color.orange);
            colorList.add(Color.MAGENTA);
            colorList.add(Color.gray.darker());
        }
        return colorList.get(stationNumber % colorList.size());
    }

    @Override
    protected void paintForeground(Graphics g, double x0, double y0, double zoom) {

        // On-screen coordinates of the center of the cell:
        final double xApp = absolutePosition.x * zoom + x0;
        final double yApp = g.getClipBounds().height - (absolutePosition.y * zoom + y0);
        final double appSize = zoom * cellSize;

        g.setColor(Color.white);
        g.fillRect((int) (xApp - appSize * 0.5), (int) (yApp - appSize * 0.5),
                (int) appSize, (int) appSize);
        g.setColor(this.color);
        g.fillRect((int) (xApp - appSize * 0.5 + 3), (int) (yApp - appSize * 0.5 + 3),
                (int) appSize - 6, (int) appSize - 6);

        int rr = this.color.getRed();
        int gg = this.color.getGreen();
        int bb = this.color.getBlue();
        int grayLevel = (rr + gg + bb);
        if (grayLevel > 255) {
            g.setColor(Color.black);
        } else {
            g.setColor(Color.white);
        }

        g.setFont(new Font("helvetica", Font.PLAIN, (int) (max(10, appSize * 0.75))));
        int textHeight = g.getFontMetrics().getHeight();
        g.drawString(stationId + "",
                (int) (xApp - appSize * 0.25 + 1),
                (int) (yApp - appSize / 2 + textHeight));
        for (Passenger p : passengerList) {
            p.paint(g, x0, y0, zoom);
        }

        super.paintForeground(g, x0, y0, zoom);
    }

    protected void addPassenger(Passenger passenger) {
        this.addPassenger(passenger, false);
    }

    protected void addPassenger(Passenger passenger, boolean shouldStopWalking) {

        if (passenger.getX() == Double.MAX_VALUE) {
            double newX = this.absolutePosition.x + (new Random().nextDouble() - 0.5) * cellSize;
            double newY = this.absolutePosition.y + (new Random().nextDouble() - 0.5) * cellSize;
            passenger.setCoordinates(newX, newY);
        }

        if (shouldStopWalking) {
            passenger.stopWalking();
        }

        this.passengerList.add(passenger);
    }

    @Override
    protected boolean hasPassengers() {
        return passengerList != null && !passengerList.isEmpty();
    }

    @Override
    protected void getPassengersOff() {
        // Find a stopped Wagon
        for (TrainElement te : trainElements) {
            if (te instanceof Wagon) {
                Wagon wagon = (Wagon) te;
                this.passengerList.addAll(wagon.dropPassengers(this.stationId));
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
    protected void addTrainElement(TrainElement newTrain) {
        super.addTrainElement(newTrain);
        if (newTrain instanceof Locomotive) {

            Locomotive newLoco = (Locomotive) newTrain;
            newLoco.addStationToLine(this);
        }
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
            computeSpeedToWalkAwayFromWalls(p);
            StationCell neighborCell = computeSpeedToWalkToNextStation(p);
            p.move(dt);
            if (hasLeftCell(p) && neighborCell != null) {
                iter.remove();
                passengersLeavingCell.add(p);
                p.validateFirstPathStep();
            }
        }
    }

    private StationCell computeSpeedToWalkToNextStation(Passenger p) {
        int otherStationId = p.getLastPathStep();
        StationCell neighborCell = null;
        for (CardinalPoint direction : walkways.keySet()) {
            neighborCell = walkways.get(direction);
            if (neighborCell != null && neighborCell.getId() == otherStationId) {
                // Passenger wants to go to neighborCell.
                switch (direction) {
                case NORTH:
                    p.setVy(1);
                    break;
                case EAST:
                    p.setVx(1);
                    break;
                case SOUTH:
                    p.setVy(-1);
                    break;
                case WEST:
                    p.setVx(-1);
                    break;
                default:
                }
            }
        }
        return neighborCell;
    }

    /**
     * Move a passenger away from the walls, but not from one specific wall if
     * the passenger is walking toward another cell.
     *
     * @param p
     */
    private void computeSpeedToWalkAwayFromWalls(Passenger p) {

        double cellX = this.absolutePosition.x;
        double cellY = this.absolutePosition.y;
        double margin = Cell.cellSize * LATERAL_MARGIN_PERCENTAGE / 100;

        if (p.getY() > cellY + cellSize / 2 - margin) {
            p.setVy(-0.5); // Move away from north wall.
        } else if (p.getY() < cellY - cellSize / 2 + margin) {
            p.setVy(0.5);  // Move away from south wall.
        } else {
            p.setVy(0);
        }

        if (p.getX() > cellX + cellSize / 2 - margin) {
            p.setVx(-0.5); // Move away from east wall.
        } else if (p.getX() < cellX - cellSize / 2 + margin) {
            p.setVx(0.5); // Move away from west wall.
        } else {
            p.setVx(0);
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

    @Override
    protected void savePassengers(FileWriter writer) {
        for (Passenger p : passengerList) {
            p.save(writer);
        }
    }

    @Override
    protected void removePassengers() {
        super.removePassengers();
        passengerList.clear();
    }

    @Override
    protected int getNbPassengers() {
        if (passengerList != null) {
            return passengerList.size();
        } else {
            return 0;
        }
    }

    protected void flushMovingPassengers() {
        passengersLeavingCell.clear();
    }

    /**
     * Tell if the station is empty.
     *
     * @return false. A station is never considered an empty cell.
     */
    @Override
    protected boolean isEmpty() {
        return false;
    }
}
