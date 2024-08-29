package minimetro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import static java.lang.Math.max;
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

    private ArrayList<Cell> openList;
    private ArrayList<Cell> closedList;
    private ArrayList<Movement> path;

    public Passenger() {
        id = NB_PASSENGERS_CREATED;
        NB_PASSENGERS_CREATED++;
        openList = new ArrayList<>();
        closedList = new ArrayList<>();
        path = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "P_" + id;
    }

    protected void paint(Graphics g, double x0, double y0, double zoom) {

        // On-screen coordinates of the center of the cell:
        final double xApp = x * zoom + x0;
        final double yApp = g.getClipBounds().height - (y * zoom + y0);
        final double appSize = zoom * size;

        g.setColor(this.color);
        g.fillOval((int) (xApp - appSize / 2), (int) (yApp - appSize / 2),
                (int) appSize, (int) appSize);
        g.setColor(Color.gray.brighter());
        g.setFont(new Font("helvetica", Font.PLAIN, (int) (max(10, appSize / 15))));
        g.drawString(id + " -> " + targetStationId, (int) (xApp), (int) (yApp));
    }

    protected void setCoordinates(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    protected void setTargetStationId(int newTargetStation) {
        targetStationId = newTargetStation;
    }

    protected int getTargetStationId() {
        return targetStationId;
    }

    /**
     * Add a cell to the open list, without duplicates.
     *
     * @param cell
     */
    protected void addToOpenList(Cell cell) {
        if (!openList.contains(cell)) {
            openList.add(cell);
        }
    }

    protected boolean openListIsEmpty() {
        return openList.isEmpty();
    }

    void setPath(Object object) {
    }

    void clearPath() {
        path.clear();
    }

    protected Cell removeFirstOpen() {
        Cell cell = openList.remove(0);
        return cell;
    }

    protected void addToClosedList(Cell cell) {
        closedList.add(cell);
    }

    protected void finalizePath() {

    }

    protected void computePath(World w, StationCell station) {

        ArrayList<Movement> movementList = w.getMovements(x, y);
    }
}
