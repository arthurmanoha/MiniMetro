package minimetro;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 *
 * @author arthu
 */
public class Passenger {

    private double x, y;
    private int targetStationId;
    private double size = 10;
    private Color color = Color.blue;
    private static int NB_PASSENGERS_CREATED = 0;
    private int id;
    private ArrayList<Integer> path;

    public Passenger() {
        id = NB_PASSENGERS_CREATED;
        NB_PASSENGERS_CREATED++;
        path = new ArrayList<>();
    }

    @Override
    public String toString() {
        String result = "P_ " + id;
        for (Integer step : path) {
            result += " " + step;
        }
        return result;
    }

    protected void paint(Graphics g, double x0, double y0, double zoom) {

        // On-screen coordinates of the center of the cell:
        final double xApp = x * zoom + x0;
        final double yApp = g.getClipBounds().height - (y * zoom + y0);
        final double appSize = zoom * size;

        g.setColor(this.color);
        g.fillOval((int) (xApp - appSize / 2), (int) (yApp - appSize / 2),
                (int) appSize, (int) appSize);
        g.setColor(Color.black);
        g.drawOval((int) (xApp - appSize / 2), (int) (yApp - appSize / 2),
                (int) appSize, (int) appSize);
        g.setColor(Color.black);
        String text = "";
        for (int step : path) {
            text += step + " ";
        }
        Font font = new Font("helvetica", Font.PLAIN, (int) appSize);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        g.setColor(Color.gray.darker());
        g.drawString(text,
                (int) (xApp - metrics.stringWidth(text) / 2),
                (int) (yApp - metrics.getHeight() / 2 + metrics.getAscent()));
    }

    protected void setCoordinates(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    protected void setTargetStationId(int newTargetStation) {
        targetStationId = newTargetStation;
        color = StationCell.getStationColor(targetStationId);
    }

    protected int getTargetStationId() {
        return targetStationId;
    }

    void clearPath() {
        path.clear();
    }

    protected void addPathStep(int newStep) {
        path.add(newStep);
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
        path.remove(0);
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
}
