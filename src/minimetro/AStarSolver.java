package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

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

    private int nbStepsMax = 50;

    private static double TURN_COST = 5.0;
    private static double HORIZ_VERTIC_COST = 1;
    private static double DIAGONAL_COST = 1.5;

    private enum LockObject {
        INSTANCE
    }

    public enum ComputationState {
        SOLUTION_FOUND,
        NO_SOLUTION,
        STILL_COMPUTING
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
        System.out.println("solver.setStart(" + row + ", " + col + ")");
        start = new PathPoint(col, row, null);
        start.gScore = 0;
        start.hScore = 0;
        openList.add(start);
    }

    public void setEnd(int row, int col) {
        System.out.println("solver.setEnd(" + row + ", " + col + ")");
        end = new PathPoint(col, row, null);
    }

    public void reset() {
        openList.clear();
        closedList.clear();
        finalPath.clear();
        openList.add(start);
        isEndReached = false;
    }

    public void fullReset() {
        reset();
        start = null;
        end = null;
        openList.clear();
    }

    public void fullSolve() {

        fullSolve(1000);
    }

    /**
     * @param maxComputationTime maximum duration allowed for this computation
     * step. If no solution was found, the search will pick up where it left
     * off.
     * @return +1 if the solution was found, -1 if there is no solution, 0 if
     * the computation is not done or stuck yet.
     */
    public int fullSolve(double maxComputationTime) {

        boolean loop = true;
        long startTime = System.currentTimeMillis();
        long currentTime = startTime;
        int result = -1;

        while (loop && (currentTime - startTime) < maxComputationTime) {
            synchronized (LockObject.INSTANCE) {
                result = step();
            }
            if (result != 0) { // result !=0 means either a solution was found or no solution exists.
                loop = false;
            }
            currentTime = System.currentTimeMillis();
        }

        return result;
    }

    private void computeFinalPath(PathPoint currentNode) {

        System.out.println("******************");
        System.out.println("*** FINAL PATH ***");
        System.out.println("******************");

        int nbTurns = 0;

        finalPath.add(currentNode);
        PathPoint p = currentNode.predecessor;
        while (p != null) {
            finalPath.add(p);
            p = p.predecessor;
            if (p != null) {
                p.isInFinalPath = true;
            }
            try {
                if (p.predecessor.computeIsStraightLine(p.predecessor.predecessor, p)) {
                    // p.predecessor is now an official straight line of the final path.
                    p.predecessor.isStraightLine = true;
                } else {
                    p.predecessor.isStraightLine = false;
                    nbTurns++;
                }
            } catch (NullPointerException e) {
                // Not all predecessors are defined; no turn is detetected at this step.
            }
        }
        System.out.println("Total " + nbTurns + " turns.");
    }

    protected void paint(Graphics g, double x0, double y0, double zoom) {
        synchronized (LockObject.INSTANCE) {
            try {
                Iterator<PathPoint> iter;

                iter = openList.iterator();
                while (iter.hasNext()) {
                    PathPoint p = iter.next();
                    p.paint(g, x0, y0, zoom, Color.blue, world.getNbRows());
                }

                iter = closedList.iterator();
                while (iter.hasNext()) {
                    PathPoint p = iter.next();
                    p.paint(g, x0, y0, zoom, Color.gray, world.getNbRows());
                }

                iter = finalPath.iterator();
                while (iter.hasNext()) {
                    PathPoint p = iter.next();
                    p.paint(g, x0, y0, zoom, Color.yellow, world.getNbRows());
                }
                if (isEndReached) {
                    // Paint solution
                    PathPoint current = end.predecessor;
                    while (current != start && current != null) {// TEST NULL
                        current.paint(g, x0, y0, zoom, Color.YELLOW, world.getNbRows());
                    }
                }

                if (start != null && start.x != Integer.MAX_VALUE) {
                    // Draw start
                    start.paint(g, x0, y0, zoom, Color.red, world.getNbRows());
                }
                if (end != null && end.x != Integer.MAX_VALUE) {
                    end.paint(g, x0, y0, zoom, Color.red, world.getNbRows());
                }
            } catch (NullPointerException e) {
                System.out.println("Exception in AStarSolver.paint()");
            } catch (ConcurrentModificationException e) {
                System.out.println("ConcurrentModificationException in AStarSolver.paint()");
            }
        }
    }

    /**
     * Compute one step of the A* algorithm.
     *
     * @return +1 if the solution was found, -1 if there is no solution, 0 if
     * the computation is not done or stuck yet.
     */
    public int step() {

        if (isEndReached) {
            return 1;
        }

        if (start == null || end == null) {
            // No start or end specified
            System.out.println("Need to specify both start and end.");
            return -1;
        }

        if (openList.isEmpty()) {
            return -1; // No solution.
        }

        // Get the lowest f-score node
        PathPoint currentNode = openList.remove(0);

        // Test if end is reached
        if (currentNode.equals(end)) {
            isEndReached = true;
            computeFinalPath(currentNode);
            return 1; // Solution found !
        }

        ArrayList<PathPoint> neighbors = computeNeighbors(currentNode);
        // For each neighbor:
        for (PathPoint neighbor : neighbors) {
            neighbor.computeHeuristic(end);

            double unitCost = computeUnitCost(currentNode, neighbor);
            if (!closedList.contains(neighbor)) {

                if (openList.contains(neighbor)) {
                    // Test if the new potential score is better

                    if (currentNode.getF() + unitCost < neighbor.getF()) {
                        // Found a better way to reach this neighbor.
                        neighbor.predecessor = currentNode;
                        neighbor.gScore = currentNode.gScore + unitCost;

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
        closedList.add(currentNode);
        openList.sort(comparator);

        return 0; // No solution found yet.
    }

    private ArrayList<PathPoint> computeNeighbors(PathPoint currentNode) {
        // This method should return the list of allowed neighbors, i.e. only
        // the cells where rails may be built, not the oceans or mountains.

        ArrayList<PathPoint> neighbors = new ArrayList<>();
        // Request the available neighbors from the terrain, i.e. the cells where rails can be built.
        for (Cell c : world.getAvailableNeighbors(currentNode.y, currentNode.x)) {
            neighbors.add(new PathPoint(c.col, c.row, currentNode));
        }

        return neighbors;
    }

    // Compute and return the cost of the way from one node to a neighbor
    private double computeUnitCost(PathPoint currentNode, PathPoint neighbor) {

        double result = 0;

        if (!currentNode.computeIsStraightLine(currentNode.predecessor, neighbor)) {
            result += TURN_COST;
        }
        if (currentNode.x == neighbor.x || currentNode.y == neighbor.y) {
            // Same row or same column
            result += HORIZ_VERTIC_COST;
        } else {
            // Different row AND different column, i.e. diagonal.
            result += DIAGONAL_COST; // YES
//            return 1.49; // YES
//            return 1.45; // NO
// "NO" values return a path with too much zig-zag
        }
        return result;
    }

    /**
     * Export the final path
     *
     * @return the final path
     */
    public ArrayList<PathPoint> getFinalPath() {
        ArrayList<PathPoint> list = new ArrayList<>();
        for (PathPoint p : finalPath) {
            list.add(p);
        }
        return list;
    }
}
