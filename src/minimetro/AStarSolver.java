package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import static minimetro.Cell.cellSize;

/**
 *
 * @author arthu
 */
public class AStarSolver {

    private World world;

    private boolean useDiagonals = true;
    private boolean isEndReached;

    private class PathPoint extends Point {

        public PathPoint predecessor;
        public boolean isInFinalPath;

        public PathPoint(int x, int y, PathPoint other) {
            super(x, y);
            this.predecessor = other;
        }

        public PathPoint() {
            this(0, 0, null);
        }
    }

    // The x-value of the point is the column, the y-value is the row.
    PathPoint start, end;
    ArrayList<PathPoint> openList, closedList, finalPath;

    public AStarSolver(World w) {
        this.world = w;
        start = new PathPoint();
        end = new PathPoint();
        openList = new ArrayList<>();
        closedList = new ArrayList<>();
        finalPath = new ArrayList<>();
    }

    public void setStart(int row, int col) {
        start = new PathPoint(col, row, null);
        openList.add(start);
    }

    public void setEnd(int row, int col) {
        end = new PathPoint(col, row, null);
    }

    public void reset() {
        openList.clear();
        closedList.clear();
        finalPath.clear();
        openList.add(start);
        isEndReached = false;
    }

    public void step() {

        if (!openList.isEmpty()) {
            // Remove the first cell of the open list
            PathPoint currentCell = openList.remove(0);
            closedList.add(currentCell);

            // Compute all the current neighbors
            for (int row = -1; row <= 1; row++) {
                for (int col = -1; col <= 1; col++) {

                    if (useDiagonals || row * col == 0) {

                        PathPoint neighbor = new PathPoint(currentCell.x + col, currentCell.y + row, currentCell);
                        if (neighbor.equals(end)) {
                            isEndReached = true;
                            end.predecessor = currentCell;
                        }
                        if (!openList.contains(neighbor) && !closedList.contains(neighbor)) {
                            // The neighbor was never processed
                            openList.add(neighbor);
                        }
                    }
                }
            }
        }
        if (isEndReached) {
            System.out.println("End reached !");
            computeFinalPath();
        }
    }

    public void fullSolve() {
        while (!isEndReached) {
            step();
        }
    }

    private void computeFinalPath() {

        finalPath.add(end);
        PathPoint p = end.predecessor;
        while (!p.equals(start)) {
            finalPath.add(p);
            p = p.predecessor;
        }
        finalPath.add(start);

        openList.clear();
        closedList.clear();

    }

    protected void paint(Graphics g, double x0, double y0, double zoom) {

        for (PathPoint p : openList) {
            drawCell(p, g, x0, y0, zoom, Color.blue);
        }
        for (PathPoint p : closedList) {
            drawCell(p, g, x0, y0, zoom, Color.gray);
        }

        for (PathPoint p : finalPath) {
            drawCell(p, g, x0, y0, zoom, Color.YELLOW);
        }

        if (start.x != Integer.MAX_VALUE) {
            // Draw start
            drawCell(start, g, x0, y0, zoom, Color.red);
        }
        if (end.x != Integer.MAX_VALUE) {
            drawCell(end, g, x0, y0, zoom, Color.green);
        }
    }

    private void drawCell(PathPoint cell, Graphics g, double x0, double y0, double zoom, Color c) {

        final double appSize = zoom * cellSize;
        double xApp = cellSize * (cell.x - 0.5) * zoom + x0;
        double yApp = g.getClipBounds().height - (cellSize * (world.getNbRows() - cell.y - 0.5) * zoom + y0);

        int margin = 0;

        // Center color is specified as a parameter
        if (cell.isInFinalPath) {
            g.setColor(Color.orange);
        } else {
            g.setColor(c);
        }
        g.fillRect((int) xApp + margin, (int) yApp + margin, (int) appSize - 2 * margin, (int) appSize - 2 * margin);
    }
}
