package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import static minimetro.Cell.cellSize;

/**
 *
 * @author arthu
 */
public class AStarSolver {

    private World world;

    private boolean useDiagonals = true;
    private boolean isEndReached;

    // The x-value of the point is the column, the y-value is the row.
    private PathPoint start, end;
    private ArrayList<PathPoint> openList, closedList, finalPath;
    private Comparator<PathPoint> comparator;

    private class PathPoint extends Point {

        public PathPoint predecessor;
        public boolean isInFinalPath;
        public double gScore, hScore;

        public PathPoint(int x, int y, PathPoint other) {
            super(x, y);
            this.predecessor = other;
            gScore = 1000;
            hScore = 1000;
        }

        public PathPoint() {
            this(0, 0, null);
        }

        private void paint(Graphics g, double x0, double y0, double zoom, Color c) {

            final double appSize = zoom * cellSize;
            double xApp = cellSize * (this.x - 0.5) * zoom + x0;
            double yApp = g.getClipBounds().height - (cellSize * (world.getNbRows() - this.y - 0.5) * zoom + y0);

            int margin = 0;

            // Center color is specified as a parameter
            if (this.isInFinalPath) {
                g.setColor(Color.orange);
            } else {
                g.setColor(c);
            }
            g.fillRect((int) xApp + margin, (int) yApp + margin, (int) appSize - 2 * margin, (int) appSize - 2 * margin);

            // Paint predecessor
            if (predecessor == null) {
                g.setColor(Color.black);
                g.drawRect(
                        (int) (xApp + appSize / 4),
                        (int) (yApp + appSize / 4),
                        (int) (appSize / 2),
                        (int) (appSize / 2));

            } else {
                g.setColor(Color.yellow);
                int scale = 20;

                int dx = predecessor.x - this.x;
                int dy = predecessor.y - this.y;
                g.drawLine((int) (xApp + appSize / 2), (int) (yApp + appSize / 2),
                        (int) (xApp + appSize / 2 + scale * dx), (int) (yApp + appSize / 2 + scale * dy));
            }
////            // Print cost
            g.setColor(Color.black);
            g.drawString("g: " + gScore, (int) xApp, (int) (yApp + appSize / 3));
            g.drawString("h: " + hScore, (int) xApp, (int) (yApp + 2 * appSize / 3));
        }

        private void computeHeuristic() {
            hScore = 1000; // Math.abs(end.x - this.x) + Math.abs(end.y - this.y);
        }

        private double getF() {
            return this.gScore + this.hScore;
        }

        @Override
        public String toString() {
            return super.toString() + ", pred " + predecessor;
        }
    }

    public AStarSolver(World w) {
        this.world = w;
        openList = new ArrayList<>();
        closedList = new ArrayList<>();
        finalPath = new ArrayList<>();
        comparator = new Comparator<PathPoint>() {
            @Override
            public int compare(PathPoint p0, PathPoint p1) {
                double scoreDifference = p0.getF() - p1.getF();
                if (scoreDifference < 0) {
                    return -1;
                } else if (scoreDifference > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
    }

    public void setStart(int row, int col) {
        start = new PathPoint(col, row, null);
        start.gScore = 0;
        start.hScore = 0;
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

    public void fullSolve() {
        while (!isEndReached) {
            step();
        }
    }

    private void computeFinalPath(PathPoint currentNode) {
        finalPath.add(currentNode);
        PathPoint p = currentNode.predecessor;
        while (p != null) {
            finalPath.add(p);
            p = p.predecessor;
        }
    }

    protected void paint(Graphics g, double x0, double y0, double zoom) {

        for (PathPoint p : openList) {
            p.paint(g, x0, y0, zoom, Color.blue);
        }
        for (PathPoint p : closedList) {
            p.paint(g, x0, y0, zoom, Color.gray);
        }
        for (PathPoint p : finalPath) {
            p.paint(g, x0, y0, zoom, Color.YELLOW);
        }
        if (isEndReached) {
            // Paint solution
            PathPoint current = end.predecessor;
            while (current != start && current != null) {// TEST NULL
                current.paint(g, x0, y0, zoom, Color.YELLOW);
            }
        }

        if (start != null && start.x != Integer.MAX_VALUE) {
            // Draw start
            start.paint(g, x0, y0, zoom, Color.red);
        }
        if (end != null && end.x != Integer.MAX_VALUE) {
            end.paint(g, x0, y0, zoom, Color.red);
        }
    }

    public void step() {

//        System.out.println("Step Start.");
        if (openList.isEmpty()) {
            System.out.println("Open list is empty.");
            return;
        }

        // Get the lowest f-score node
        PathPoint currentNode = openList.remove(0);

        // Test if end is reached
        if (currentNode.equals(end)) {
            isEndReached = true;
            computeFinalPath(currentNode);
            return;
        }

        ArrayList<PathPoint> neighbors = computeNeighbors(currentNode);
        // For each neighbor:
        for (PathPoint neighbor : neighbors) {
            neighbor.computeHeuristic();

            double unitCost = computeUnitCost(currentNode, neighbor);
            if (!closedList.contains(neighbor)) {

                if (openList.contains(neighbor)) {
                    // Test if the new potential score is better
//                    System.out.println("current F + unit: " + (currentNode.getF() + 1)
//                            + ", neighbor F: " + neighbor.getF());

                    if (currentNode.getF() + unitCost > neighbor.getF()) {
                        System.out.println("            BETTER PATH");
                        // Found a better way to reach this neighbor.
                        neighbor.predecessor = currentNode;
//                        System.out.println("neighbor.gScore was " + neighbor.gScore);
                        neighbor.gScore = currentNode.gScore + unitCost;
//                        System.out.println("neighbor.gScore is now " + neighbor.gScore);

                        // TEST START
                        if (!openList.contains(neighbor)) {
                            openList.add(neighbor);
                        }
                        // TEST END

                    }
                } else {
                    // Neighbor not in open list, we add it.
                    if (!closedList.contains(neighbor)) { // TEST - necessary ???
                        openList.add(neighbor);
                    }
                }
                neighbor.gScore = currentNode.gScore + unitCost; // ???
            }
        }
//        if (!isEndReached) {
        closedList.add(currentNode);
        openList.sort(comparator);

        // Test list sorting
//        }
//        System.out.println("Step End");
    }

    private ArrayList<PathPoint> computeNeighbors(PathPoint currentNode) {
        // This method should return the list of allowed neighbors, i.e. only
        // the cells where rails may be built, not the oceans or mountains.

        ArrayList<PathPoint> neighbors = new ArrayList<>();

        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow != 0 || dCol != 0) {
                    neighbors.add(new PathPoint(currentNode.x + dCol, currentNode.y + dRow, currentNode));
                }
            }
        }
//        System.out.println("Computed " + neighbors.size() + " neighbors.");
        return neighbors;
    }

    // Compute and return the cost of the way from one node to a neighbor
    private double computeUnitCost(PathPoint currentNode, PathPoint neighbor) {
        if (currentNode.x == neighbor.x || currentNode.y == neighbor.y) {
            // Same row or same column
            return 1;
        } else {
            // Different row AND different column, i.e. diagonal.
            return 1.414;
        }
    }
}
