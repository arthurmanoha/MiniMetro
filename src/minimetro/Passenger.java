package minimetro;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author arthu
 */
public class Passenger extends RelativisticElement {

    private double vx, vy;
    private double vMax;
    private int targetStationId;
    private double size = 1;
    private Color color;
    private static int NB_PASSENGERS_CREATED = 0;
    protected int id;
    private ArrayList<Integer> path; // First entry is the destination, last entry is the next stop.

    public Passenger() {
        this(-1, Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public Passenger(int newId, double newX, double newY) {
        this(newId, -1, newX, newY);
    }

    public Passenger(int targetStationId) {
        this(-1, targetStationId, Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public Passenger(int newId, int newTargetStationId, double newX, double newY) {
        super(newId, null);
        if (newId > NB_PASSENGERS_CREATED) {
            id = newId;
            NB_PASSENGERS_CREATED = newId;
        } else {
            id = NB_PASSENGERS_CREATED;
            NB_PASSENGERS_CREATED++;
        }
        path = new ArrayList<>();
        setPosition(new Point2D.Double(newX, newY));
        if (newTargetStationId >= 0) {
            setTargetStationId(newTargetStationId);
        }
        vx = 0;
        vy = 0;
        vMax = 10;
        this.color = StationCell.getStationColor(newTargetStationId);
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom) {
        paint(g, x0, y0, zoom, 0);
    }

    @Override
    public void paint(Graphics g, double x0, double y0, double zoom, double delay) {

        try {
            // On-screen coordinates of the center of the cell:
            Point2D.Double position = getAbsolutePosition((int) delay);
            final double xApp = position.x * zoom + x0;
            final double yApp = g.getClipBounds().height - (position.y * zoom + y0);
            final double appSize = zoom * size;

            g.setColor(this.color);
            g.fillOval((int) (xApp - appSize / 2), (int) (yApp - appSize / 2),
                    (int) appSize, (int) appSize);
            g.setColor(Color.black);
            g.drawOval((int) (xApp - appSize / 2), (int) (yApp - appSize / 2),
                    (int) appSize, (int) appSize);
            String text = "";
            for (int step : path) {
                text += step + " ";
            }
            if (path.isEmpty()) {
                text += "_";
            }
            Font font = new Font("helvetica", Font.PLAIN, (int) appSize / 2);
            g.setFont(font);
            FontMetrics metrics = g.getFontMetrics(font);
            g.setColor(Color.black);
            g.drawString(text,
                    (int) (xApp - metrics.stringWidth(text) / 2),
                    (int) (yApp - metrics.getHeight() / 2 + metrics.getAscent()));
        } catch (RelativityException | NullPointerException e) {
            // Do not paint anything.
        }
    }

    protected double getVx() {
        return this.vx;
    }

    protected double getVy() {
        return this.vy;
    }

    protected final void setTargetStationId(int newTargetStation) {
        targetStationId = newTargetStation;
        computeColor(newTargetStation);
    }

    private void computeColor(int stationId) {
        color = StationCell.getStationColor(stationId);
    }

    protected int getTargetStationId() {
        return targetStationId;
    }

    void clearPath() {
        path.clear();
    }

    protected void addPathStep(int newStep) {
        path.add(newStep);
        computeColor(path.get(0));
    }

    protected int getFirstPathStep() {
        if (path.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return path.get(0);
    }

    protected int getLastPathStep() {
        if (path.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return path.get(path.size() - 1);
    }

    protected void validateFirstPathStep() {
        if (!path.isEmpty()) {
            path.remove(0);
        }
    }

    protected String getItineraryToString() {
        String res = "";
        for (int stationId : path) {
            res += stationId + " ";
        }
        return res;
    }

    /**
     * Remove the given station id from path
     *
     * @param stationId
     */
    protected void removeStationFromPath(int stationId) {
        path.remove((Integer) stationId);
    }

    protected void setSpeed(double newVx, double newVy) {
        vx = newVx * vMax;
        vy = newVy * vMax;
    }

    protected void setVx(double newVx) {
        vx = newVx * vMax;
    }

    protected void setVy(double newVy) {
        vy = newVy * vMax;
    }

    protected void move(double dt) {
        double newX = getX() + vx * dt;
        double newY = getY() + vy * dt;
        this.setPosition(new Point2D.Double(newX, newY));
    }

    protected void stopWalking() {
        vx = 0;
        vy = 0;
    }

    @Override
    public String toString() {
        String result = World.PASSENGER + " " + id + " " + this.targetStationId
                + " " + this.getX() + " " + this.getY();
        for (Integer step : path) {
            result += " " + step;
        }
        return result;
    }

    protected void save(FileWriter writer) {
        try {
            String text = this.toString() + "\n";
            writer.write(text);
        } catch (IOException e) {
            System.out.println("Error writing Passenger to file.");
        }
    }

    @Override
    protected void updateHistory() {
        super.updateHistory();
    }
}
