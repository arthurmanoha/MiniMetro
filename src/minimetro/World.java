package minimetro;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class represents the terrain that contains the tracks, trains,
 * landscape, ...
 *
 * @author arthu
 */
public class World {

    private int nbRows, nbCols;
    private ArrayList<ArrayList<Cell>> cells;
    private double dt; // Evolution step

    private ArrayList<TrainElement> trainsInTransition;

    private Timer timer;
    private boolean isRunning;
    private int periodMillisec;
    private int step;

    // Tell observers that our state has changed.
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public World() {
        nbRows = 150;
        nbCols = 240;
        cells = new ArrayList<>();
        for (int row = 0; row < nbRows; row++) {
            cells.add(new ArrayList<>());
            for (int col = 0; col < nbCols; col++) {
                cells.get(row).add(new Cell());
            }
        }
        dt = 0.1;
        trainsInTransition = new ArrayList<>();
        step = 0;
        isRunning = false;
        periodMillisec = 100;
        startTimer();
    }

    public int getNbRows() {
        return nbRows;
    }

    public int getNbCols() {
        return nbCols;
    }

    public Cell getCell(int row, int col) {
        try {
            return cells.get(row).get(col);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void togglePlayPause() {
        isRunning = !isRunning;

        if (isRunning) {
            System.out.println("World: play");
        } else {
            System.out.println("World: pause");
        }
    }

    public void step() {
        System.out.println("Start step");
        for (ArrayList<Cell> row : cells) {
            for (Cell c : row) {
                c.evolve(dt);
            }
        }
        System.out.println("after all cells evolve");

        // Transfert trains between cells when necessary
        int rowIndex = 0;
        for (ArrayList<Cell> row : cells) {
            int colIndex = 0;
            for (Cell c : row) {
                if (c.isTrainElementSwitchingCells) {
                    TrainElement movingTrain = c.removeTrain();
                    System.out.println("Moving train with heading " + movingTrain.getHeading());
                    reinsertTrain(movingTrain, rowIndex, colIndex);
                    c.isTrainElementSwitchingCells = false;
                }
                colIndex++;
            }
            rowIndex++;
        }

        step++;

        updateListeners();
        System.out.println("End step");
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Notify the listeners that the world's state has changed.
     *
     */
    protected void updateListeners() {
        support.firePropertyChange("currentStep", step - 1, step);
    }

    protected void setCell(int row, int col, Cell newCell) {
        try {
            cells.get(row).set(col, newCell);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("World.setCell: error");
        }
    }

    protected void toggleStation(int row, int col) {
        Cell oldCell = this.getCell(row, col);
        if (oldCell instanceof StationCell) {
            this.setCell(row, col, new Cell());
        } else {
            this.setCell(row, col, new StationCell());
        }
    }

    /**
     * Add a locomotive if the designated cell has rails.
     *
     * @param row
     * @param col
     */
    protected void addLoco(int row, int col) {
        Cell c = getCell(row, col);
        c.setLoco();
    }

    /**
     * Add a carriage to the end of the train under construction.
     *
     * @param row
     * @param col
     */
    protected void addWagon(int row, int col) {
        Cell c = getCell(row, col);
        c.setWagon();
    }

    /**
     * The previous cell must create a track between the current and prevPrev
     * cells.
     */
    protected void setNewTrack(int colBefore, int rowBefore, int colCenter, int rowCenter, int colAfter, int rowAfter) {
        Cell newTrackCell = getCell(rowCenter, colCenter);

        int dxBefore = colBefore - colCenter;
        int dyBefore = rowBefore - rowCenter;
        int dxAfter = colAfter - colCenter;
        int dyAfter = rowAfter - rowCenter;

        try {
            newTrackCell.setTrack(dxBefore, dyBefore, dxAfter, dyAfter);
        } catch (NullPointerException e) {
            System.out.println("Cannot place track outside world");
        }
    }

    /**
     * When a train has left its previous cell, it needs to be placed into the
     * next cell.
     *
     */
    private void reinsertTrain(TrainElement movingTrain, int rowIndex, int colIndex) {
        double currentHeading = movingTrain.headingDegrees;
        System.out.println("reinsert train heading " + currentHeading);
        int newRow = rowIndex;
        int newCol = colIndex;
        if (headingIsCloseTo(currentHeading, 0)) {
            // North
            newRow--;
            movingTrain.setHeadingDegrees(0);
        } else if (headingIsCloseTo(currentHeading, 90)) {
            // East
            newCol++;
            movingTrain.setHeadingDegrees(90);
        } else if (headingIsCloseTo(currentHeading, 180)) {
            // South
            newRow++;
            movingTrain.setHeadingDegrees(180);
        } else if (headingIsCloseTo(currentHeading, 270)) {
            // West
            newCol--;
            movingTrain.setHeadingDegrees(270);
        } else {
            System.out.println("World: direction unknown.");
        }

        // Add the train to the new cell.
        System.out.println("current cell: " + rowIndex + ", " + colIndex + ", next: " + newRow + ", " + newCol);
        Cell newCell = this.getCell(newRow, newCol);
        if (newCell == null) {
            System.out.println("next cell is null");
        } else {
            System.out.println("World.reinsertTrain before cell, speed is " + movingTrain.currentSpeed);
            TrainElement insertionCheck = newCell.addTrainElement(movingTrain);
            System.out.println("World.reinsertTrain after cell, speed is " + movingTrain.currentSpeed);
            if (insertionCheck != null) {
                // Error in train reinsertion.
                System.out.println("World: error in train reinsertion.");
            }
        }
    }

    /**
     * Compare a heading to another with a 10 degrees margin
     *
     * @return
     */
    private boolean headingIsCloseTo(double h0, int h1) {
        double limit = 10;
        return (Math.abs(h0 - h1) < limit);
    }

    private void startTimer() {
        System.out.println("Starting timer.");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isRunning) {
                    step();
                }
            }
        }, 0, periodMillisec);
    }

}
