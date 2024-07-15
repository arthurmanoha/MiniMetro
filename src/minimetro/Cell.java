package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * This is a single 1x1 element of the grid.
 * It may contain one piece of track, one carriage.
 *
 * @author arthu
 */
public class Cell {

    protected Color color;

    private ArrayList<TrainElement> trainElements;
    protected ArrayList<TrainElement> trainsInTransition;
    private boolean isLinkedNorth, isLinkedEast, isLinkedSouth, isLinkedWest;
    protected double xTop, yTop, width;

    private int id;
    private static int NB_CELLS_CREATED = 0;

    public Cell(double newX, double newY, double newWidth) {
        color = Color.gray;
        trainElements = new ArrayList<>();
        trainsInTransition = new ArrayList<>();
        isLinkedNorth = false;
        isLinkedEast = false;
        isLinkedSouth = false;
        isLinkedWest = false;
        xTop = newX;
        yTop = newY;
        width = newWidth;

        id = NB_CELLS_CREATED;
        NB_CELLS_CREATED++;
    }

    /**
     * Paint the cell with its background and foreground.
     */
    protected void paint(Graphics g, double x0, double y0, double zoom) {
        final int xApp = (int) (xTop * zoom + x0);
        final int yApp = (int) (yTop * zoom + y0);
        int appSize = (int) (width * zoom) + 1;

        // Draw background
        g.setColor(this.color);
        g.fillRect(xApp, yApp, (int) zoom + 1, (int) zoom + 1);

        // Draw borders
        g.setColor(Color.black);
        g.drawLine(xApp, yApp, xApp + appSize, yApp);
        g.drawLine(xApp, yApp, xApp, yApp + appSize);

        // Draw rails
        paintRails(g, xApp, yApp, appSize);

    }

    /**
     * Paint the trainElements alone.
     * (to be used after all cells were drawn).
     */
    protected void paintTrains(Graphics g, int row, int col, double x0, double y0, double zoom) {
        if (hasTrain()) {
            final int xApp = (int) (col * zoom + x0);
            final int yApp = (int) (row * zoom + y0);
            int appSize = (int) (width * zoom) + 1;

            // Draw train
            for (TrainElement te : trainElements) {
                te.paint(g, xApp, yApp, appSize);
            }
        }
    }

    /**
     * Set a track section that goes through this cell and remove any previously
     * existing track sections in this cell.
     */
    protected void setTrack(int dxBefore, int dyBefore, int dxAfter, int dyAfter) {

        isLinkedNorth = false;
        isLinkedEast = false;
        isLinkedSouth = false;
        isLinkedWest = false;

        // Create rail sections
        if (dxBefore != dxAfter && dyBefore != dyAfter) {
            // This is a turn.
            if (dxBefore > 0 && dyAfter < 0 || dxAfter > 0 && dyBefore < 0) {
                // Link between North and East.
                isLinkedNorth = true;
                isLinkedEast = true;
            } else if (dxBefore > 0 && dyAfter > 0 || dxAfter > 0 && dyBefore > 0) {
                // Link between East and South.
                isLinkedEast = true;
                isLinkedSouth = true;
            } else if (dxBefore < 0 && dyAfter > 0 || dxAfter < 0 && dyBefore > 0) {
                // Link between South and West.
                isLinkedSouth = true;
                isLinkedWest = true;
            } else if (dxBefore < 0 && dyAfter < 0 || dxAfter < 0 && dyBefore < 0) {
                // Link between West and North.
                isLinkedWest = true;
                isLinkedNorth = true;
            } else {
                System.out.println("Cell.setTrack: Error in quadrant selection.");
            }
        } else {
            // This is a straight line.
            if (dxBefore != dxAfter) {
                isLinkedWest = true;
                isLinkedEast = true;
            } else {
                isLinkedNorth = true;
                isLinkedSouth = true;
            }
        }
    }

    /**
     * return a value of 0, 0.5 or 1 depending on where the neighbor cell is
     * located.
     *
     * @param dxy
     * @return
     */
    private double defineBorderCoordinates(double dxy) {
        if (dxy == -1) {
            return 0;
        } else if (dxy == 0) {
            return 0.5;
        } else {
            return 1;
        }
    }

    // A cell has tracks if it is linked to at least one neighbor.
    protected boolean hasRails() {
        return isLinkedNorth || isLinkedEast || isLinkedSouth || isLinkedWest;
    }

    /**
     * Add a locomotive to the cell
     */
    protected void createNewLoco() {
        createNewTrainElement(true);
    }

    /**
     * Add a new Wagon to the cell.
     */
    protected void createNewWagon() {
        createNewTrainElement(false);
    }

    /**
     * Add a new TrainElement which is either a Loco or a Wagon.
     *
     * @param isLoco if true add a loco, else add a wagon.
     */
    private void createNewTrainElement(boolean isLoco) {
        if (hasRails()) {
            double x = 0, y = 0, vx = 0, vy = 0;
            if (isLinkedNorth) {
                x = 0.5;
                y = 0.2;
                vx = 0;
                vy = -1;
            } else if (isLinkedEast) {
                x = 0.7;
                y = 0.5;
                vx = 1;
                vy = 0;
            } else if (isLinkedSouth) {
                x = 0.5;
                y = 0.7;
                vx = 0;
                vy = 1;
            } else if (isLinkedWest) {
                x = 0.2;
                y = 0.5;
                vx = -1;
                vy = 0;
            }
            TrainElement newElem;
            if (isLoco) {
                newElem = new Locomotive(x, y, vx, vy);
            } else {
                newElem = new Wagon(x, y, vx, vy);
            }
            this.trainElements.add(newElem);
        }
    }

    /**
     * Add a new TrainElement coming from the next cell with a predetermined
     * heading.
     *
     * @param newTrain
     * @return null if everything went OK, or the train if it could not be
     * reinserted.
     */
    protected TrainElement addTrainElement(TrainElement newTrain) {

        if (!hasRails()) {
            System.out.println("No rails, cannot add train.");
            return newTrain;
        }
        trainElements.add(newTrain);

        return null;
    }

    protected boolean hasLoco() {

        if (trainElements.isEmpty()) {
            return false;
        }
        for (TrainElement te : trainElements) {
            if (te instanceof Locomotive) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasWagon() {
        if (trainElements.isEmpty()) {
            return false;
        }
        for (TrainElement te : trainElements) {
            if (te instanceof Wagon) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasTrain() {
        return hasLoco() || hasWagon();
    }

    /**
     * Return the first Locomotive in this cell, or NULL.
     *
     * @return
     */
    protected Locomotive getLoco() {
        if (hasLoco()) {
            for (TrainElement te : trainElements) {
                if (te instanceof Locomotive) {
                    return (Locomotive) te;
                }
            }
        }
        return null;
    }

    /**
     * Remove and return the trainElement if it exists, null otherwise.
     *
     * @return the trainElement if it exists, null otherwise.
     */
    protected TrainElement removeTrain() {
        if (this.hasTrain()) {
            TrainElement te = trainElements.remove(0);
            return te;
        } else {
            return null;
        }
    }

    protected void clearTrainsInTransition() {
        trainsInTransition.clear();
    }

    protected void evolve(double dt) {
        for (TrainElement te : trainElements) {
            // Move each trainElement
            double oldX = te.x;
            double oldY = te.y;
            double newX = te.x + te.vx * dt;
            double newY = te.y + te.vy * dt;

            // If the element has crossed the center (i.e. TBD), change the speed and adapt the position
            boolean xPassedCenter = (newX - 0.5) * (oldX - 0.5) < 0;
            boolean yPassedCenter = (newY - 0.5) * (oldY - 0.5) < 0;

            if (xPassedCenter) {
                boolean isLeavingRails = (te.vx > 0 && newX > 0.5 && !isLinkedEast)
                        || (te.vx < 0 && newX < 0.5 && !isLinkedWest);
                if (isLeavingRails) {
                    // Need to go North or South.
                    if (isLinkedNorth) {
                        // Go North
                        te.vy = -Math.abs(te.vx);
                        te.vx = 0;
                        newX = te.y;
                        newY = 0.5;
                    } else if (isLinkedSouth) {
                        // Go South
                        te.vy = Math.abs(te.vx);
                        te.vx = 0;
                        newX = te.y;
                        newY = 0.5;
                    }
                }
            }

            if (yPassedCenter) {
                boolean isLeavingRails = (te.vy > 0 && newY > 0.5 && !isLinkedSouth)
                        || (te.vy < 0 && newY < 0.5 && !isLinkedNorth);
                if (isLeavingRails) {
                    // Need to go East or West
                    if (isLinkedEast) {
                        // Go East
                        te.vx = Math.abs(te.vy);
                        te.vy = 0;
                        newX = 0.5;
                        newY = te.x;
                    } else if (isLinkedWest) {
                        // Go West
                        te.vx = -Math.abs(te.vy);
                        te.vy = 0;
                        newX = 0.5;
                        newY = te.x;
                    }
                }
            }

            te.x = newX;
            te.y = newY;

            // Cell change
            double margin = 0.01;
            if (newX > 1 - margin || newX < 0 + margin || newY > 1 - margin || newY < 0 + margin) {
                trainsInTransition.add(te);
            }
        }

        // Elements in transition must be removed from main list
        for (TrainElement te : trainsInTransition) {
            trainElements.remove(te);
        }

    }

    private void paintRails(Graphics g, int xApp, int yApp, int appSize) {
        g.setColor(Color.black);
        if (isLinkedNorth) {
            g.drawLine(xApp + appSize / 2, yApp + appSize / 2, xApp + appSize / 2, yApp);
        }
        if (isLinkedSouth) {
            g.drawLine(xApp + appSize / 2, yApp + appSize / 2, xApp + appSize / 2, yApp + appSize);
        }
        if (isLinkedEast) {
            g.drawLine(xApp + appSize / 2, yApp + appSize / 2, xApp + appSize, yApp + appSize / 2);
        }
        if (isLinkedWest) {
            g.drawLine(xApp + appSize / 2, yApp + appSize / 2, xApp, yApp + appSize / 2);
        }
    }
}
