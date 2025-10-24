package minimetro;

import colorramp.ColorRamp;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.floor;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 *
 * @author arthu
 */
public class WorldPanel extends JPanel implements MouseListener,
        MouseMotionListener, MouseWheelListener, PropertyChangeListener {

    private static boolean DISPLAY_ACTIVE_CELLS_BORDERS = true;
    private static String PAN_LEFT = "PAN_LEFT";
    private static String PAN_RIGHT = "PAN_RIGHT";
    private static String PAN_UP = "PAN_UP";
    private static String PAN_DOWN = "PAN_DOWN";
    private static String ZOOM_IN = "ZOOM_IN";
    private static String ZOOM_OUT = "ZOOM_OUT";
    private int panSize = 50;

    World world;
    private double zoomLevel;
    private final double zoomLevelFactor = 1.1;
    private double x0, y0;
    GuiTool currentTool;
    private int prevMouseX, prevMouseY;

    // Memorize the current cell and the last two, necessary to create rails.
    private int prevCol, prevRow;
    private double currentCol, currentRow;
    private double cornerMargin; // percentage of cell that is considered its corner

    private int graphicsCurrentHeight;
    private int graphicsCurrentWidth;

    private boolean ctrlIsPressed;
    private CardinalPoint currentDirection;
    private int straightOriginRow;
    private int straightOriginCol;

    private ArrayList<ColorRamp> allColorRamps;
    private boolean mustDisplayTerrain;

    private Color defaultBackgroundColor;

    private AStarSolver solver;
    private Thread astarThread;

    public WorldPanel(World w) {
        super();
        setSize(new Dimension(800, 600));
        world = w;
        zoomLevel = 0.0293;
        x0 = 83;
        y0 = 49;
        currentTool = GuiTool.NO_TOOL;
        prevMouseX = 0;
        prevMouseY = 0;
        prevRow = Integer.MAX_VALUE;
        prevCol = Integer.MAX_VALUE;
        cornerMargin = 0.25;

        graphicsCurrentHeight = 0;
        graphicsCurrentWidth = 0;
        ctrlIsPressed = false;
        currentDirection = null;

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        loadColorRamps();

        defaultBackgroundColor = Color.gray;

        mustDisplayTerrain = true;

        solver = new AStarSolver(world);

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), PAN_LEFT);
        getActionMap().put(PAN_LEFT, panLeft);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), PAN_RIGHT);
        getActionMap().put(PAN_RIGHT, panRight);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), PAN_UP);
        getActionMap().put(PAN_UP, panUp);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), PAN_DOWN);
        getActionMap().put(PAN_DOWN, panDown);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0), ZOOM_IN);
        getActionMap().put(ZOOM_IN, zoomIn);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0), ZOOM_OUT);
        getActionMap().put(ZOOM_OUT, zoomOut);
    }

    private Action panLeft = new AbstractAction(PAN_LEFT) {
        @Override
        public void actionPerformed(ActionEvent e) {
            x0 += panSize * zoomLevel;
            repaint();
        }
    };

    private Action panRight = new AbstractAction(PAN_RIGHT) {
        @Override
        public void actionPerformed(ActionEvent e) {
            x0 -= panSize * zoomLevel;
            repaint();
        }
    };

    private Action panUp = new AbstractAction(PAN_UP) {
        @Override
        public void actionPerformed(ActionEvent e) {
            y0 -= panSize * zoomLevel;
            repaint();
        }
    };

    private Action panDown = new AbstractAction(PAN_DOWN) {
        @Override
        public void actionPerformed(ActionEvent e) {
            y0 += panSize * zoomLevel;
            repaint();
        }
    };

    private Action zoomIn = new AbstractAction(ZOOM_IN) {
        @Override
        public void actionPerformed(ActionEvent e) {
            zoomLevel *= zoomLevelFactor;
            int xMid = getWidth() / 2;
            int yMid = getHeight() / 2;
//            x0F - xMid = k*(x0I - xMid);
            x0 = zoomLevelFactor * (x0 - xMid) + xMid;
            y0 = zoomLevelFactor * (y0 - yMid) + yMid;

            System.out.println("zoom in");
            repaint();
        }
    };

    private Action zoomOut = new AbstractAction(ZOOM_OUT) {
        @Override
        public void actionPerformed(ActionEvent e) {
            zoomLevel /= zoomLevelFactor;
            int xMid = getWidth() / 2;
            int yMid = getHeight() / 2;
            x0 = (x0 - xMid) / zoomLevelFactor + xMid;
            y0 = (y0 - yMid) / zoomLevelFactor + yMid;

            System.out.println("zoom out");
            repaint();
        }
    };

    @Override
    public void paintComponent(Graphics g) {
//        System.out.println("x0: " + x0 + ", y0: " + y0 + ", zoom: " + zoomLevel);

        graphicsCurrentWidth = g.getClipBounds().width;
        graphicsCurrentHeight = g.getClipBounds().height;

        // Erase the whole panel
        g.setColor(defaultBackgroundColor);
        g.fillRect(0, 0, graphicsCurrentWidth, graphicsCurrentHeight);

        for (int row = getMinVisibleRow(); row <= getMaxVisibleRow(); row++) {
            for (int col = getMinVisibleCol(); col <= getMaxVisibleCol(); col++) {
                Cell c = world.getCell(row, col);
                if (c.color == null) {
                    // The color of this cell is defined once now.
                    c.color = computeCellColor(c.altitude, c.biome);
                }
                Color color = c.color;
                if (mustDisplayTerrain) {
                    c.paintBackground(g, x0, y0, zoomLevel, color);
                }
            }
        }
        for (int row = getMinVisibleRow(); row <= getMaxVisibleRow(); row++) {
            for (int col = getMinVisibleCol(); col <= getMaxVisibleCol(); col++) {
                Cell c = world.getCell(row, col);
                c.paintForeground(g, x0, y0, zoomLevel);
            }
        }

        if (DISPLAY_ACTIVE_CELLS_BORDERS) {
            for (Cell c : world.activeCells) {
                c.outlineCell(g, x0, y0, zoomLevel);
            }
        }

        if (!mustDisplayTerrain) {
            paintCellBorders(g);
        }
        paintStraightTracksPossibilities(g);

        // Paint the train links
        paintTrainLinks(g, x0, y0, zoomLevel);

        // Paint the trains
        for (Cell c : world.getAllCells()) {
            c.paintTrains(g, x0, y0, zoomLevel);
        }

        // Draw origin
        g.setColor(Color.orange);
        g.drawLine((int) x0, (int) (graphicsCurrentHeight - y0), (int) (x0 + 50), (int) (graphicsCurrentHeight - y0));
        g.drawLine((int) x0, (int) (graphicsCurrentHeight - y0), (int) x0, (int) (graphicsCurrentHeight - y0 - 50));

        // Draw the biomeCenters
        if (world.hasBiomes()) {
            for (Point p : world.getBiomeCenters()) {

                double xCenter = (int) (x0 + zoomLevel * p.x);
                double yCenter = g.getClipBounds().height - (int) (y0 + zoomLevel * p.y);
                g.setColor(Color.RED);
                int size = 3;
                g.fillRect((int) (xCenter - size), (int) (yCenter - size), (int) (2 * size), (int) (2 * size));
                g.setColor(Color.BLACK);
                g.drawRect((int) (xCenter - size), (int) (yCenter - size), (int) (2 * size), (int) (2 * size));
            }
        }

        // Draw the path found by the solver
        solver.paint(g, x0, y0, zoomLevel);
    }

    private void paintStraightTracksPossibilities(Graphics g) {
        if (world.isSettingLongDistanceTracks()) {
            int startRow = world.getLongDistanceTrackRow();
            int startCol = world.getLongDistanceTrackCol();

            int appSize = (int) (zoomLevel * Cell.cellSize);

            g.setColor(Color.red);
            // Paint the authorized row and the two authorized diagonals.
            for (int col = getMinVisibleCol(); col <= getMaxVisibleCol(); col++) {

                int xApp = (int) (x0 + ((col - 0.5) * Cell.cellSize) * zoomLevel);

                // Horizontal
                int yApp = (int) (graphicsCurrentHeight - (y0 + (world.getNbRows() - startRow - 0.5) * Cell.cellSize * zoomLevel));
                g.drawRect(xApp, yApp, appSize, appSize);

                // First diagonal
                int row = startRow - startCol + col;
                yApp = (int) (graphicsCurrentHeight - (y0 + (world.getNbRows() - row - 0.5) * Cell.cellSize * zoomLevel));
                g.drawRect(xApp, yApp, appSize, appSize);

                // Second diagonal
                row = startRow + startCol - col;
                yApp = (int) (graphicsCurrentHeight - (y0 + (world.getNbRows() - row - 0.5) * Cell.cellSize * zoomLevel));
                g.drawRect(xApp, yApp, appSize, appSize);
            }
            // Paint the authorized column.
            for (int row = getMinVisibleRow(); row <= getMaxVisibleRow(); row++) {
                int xApp = (int) (x0 + ((startCol - 0.5) * Cell.cellSize) * zoomLevel);
                int yApp = (int) (graphicsCurrentHeight - (y0 + (world.getNbRows() - row - 0.5) * Cell.cellSize * zoomLevel));

                g.drawRect(xApp, yApp, appSize, appSize);
            }
        }
    }

    private int getMinVisibleCol() {
        return (int) max(0, getCol(0));
    }

    private int getMaxVisibleCol() {
        return (int) min(world.getNbCols() - 1, getCol(graphicsCurrentWidth));
    }

    private int getMinVisibleRow() {
        return (int) (max(0, getRow(0)));
    }

    private int getMaxVisibleRow() {
        return (int) min(world.getNbRows() - 1, getRow(graphicsCurrentHeight));
    }

    protected void paintCellBorders(Graphics g) {
        int gWidth = g.getClipBounds().width;
        int colMinVisible = getMinVisibleCol();
        int colMaxVisible = getMaxVisibleCol();
        int rowMinVisible = getMinVisibleRow();
        int rowMaxVisible = getMaxVisibleRow();
        int xApp, yApp;

        // The vertical and horizontal lines shall not extend beyond the borders of the world.
        int xMin = (int) (x0 - 0.5 * Cell.cellSize * zoomLevel);
        int xMax = (int) (x0 + (world.getNbCols() - 0.5) * Cell.cellSize * zoomLevel);
        int yMin = (int) (graphicsCurrentHeight - (-0.5 * Cell.cellSize * zoomLevel + y0));
        int yMax = (int) (graphicsCurrentHeight - ((world.getNbRows() - 0.5) * Cell.cellSize * zoomLevel + y0));

        g.setColor(Color.black);

        // Vertical lines
        for (int col = colMinVisible; col <= colMaxVisible; col++) {
            double xCol = (col - 0.5) * Cell.cellSize;
            xApp = (int) (x0 + xCol * zoomLevel);
            g.drawLine(xApp, (int) max(0, yMin), xApp, (int) max(0, yMax));
        }

        // Horizontal lines
        for (int row = rowMinVisible; row <= rowMaxVisible; row++) {
            double yRow = (world.getNbRows() - row - 0.5) * Cell.cellSize;
            yApp = (int) (graphicsCurrentHeight - (y0 + yRow * zoomLevel));
            g.drawLine((int) max(0, xMin), yApp, (int) min(xMax, gWidth), yApp);
        }
    }

    void paintTrainLinks(Graphics g, double x0, double y0, double zoom) {
        for (TrainLink link : world.links) {
            link.paint(g, x0, y0, zoom);
        }
    }

    void setTool(GuiTool newTool) {
        currentTool = newTool;
        if (currentTool != GuiTool.LONG_DISTANCE_TRACKS) {
            world.cancelLongDistanceTracks();
            repaint();
        }
    }

    /**
     * Increase the apparent size of the world.
     *
     */
    void zoomIn() {
        zoomLevel *= zoomLevelFactor;
    }

    /**
     * Decrease the apparent size of the world.
     *
     */
    void zoomOut() {
        zoomLevel /= zoomLevelFactor;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int b1 = MouseEvent.BUTTON1_DOWN_MASK;

        double xReal = (e.getX() - x0) / zoomLevel;
        double yReal = (graphicsCurrentHeight - e.getY() - y0) / zoomLevel;

        currentRow = floor(getRow(e.getY()));
        currentCol = floor(getCol(e.getX()));
        double currentRowDouble = getRow(e.getY());
        double currentColDouble = getCol(e.getX());

        if ((e.getModifiersEx() & b1) == b1) {

            // Clicked first mouse button
            switch (currentTool) {
            case LOCO -> {
                world.addLoco(xReal, yReal);
            }
            case WAGON -> {
                world.addWagon(xReal, yReal);
            }
            case TRAIN_REMOVAL -> {
                world.removeTrains((int) currentRow, (int) currentCol);
                repaint();
            }
            case TRACK -> {
                prevCol = Integer.MAX_VALUE;
                prevRow = Integer.MAX_VALUE;
            }
            case LONG_DISTANCE_TRACKS -> {
                world.setLongDistanceTracks((int) currentRow, (int) currentCol);
                repaint();
            }
            case SWITCH -> {
                world.setSwitchPoint(currentRowDouble, currentColDouble);
                repaint();
            }
            case STATION -> {
                world.toggleStation((int) currentRow, (int) currentCol);
                repaint();
            }
            case SPEED_INDICATOR -> {
                world.setSpeedIndicator((int) currentRow, (int) currentCol);
            }
            case STOP_TIMER -> {
                world.setStopTimer((int) currentRow, (int) currentCol);
            }
            case A_STAR_START -> {
                solver.setStart((int) currentRow, (int) currentCol);
                repaint();
            }
            case A_STAR_END -> {
                solver.setEnd((int) currentRow, (int) currentCol);
                repaint();
            }
            }
        }
        int b2 = MouseEvent.BUTTON3_DOWN_MASK;
        if ((e.getModifiersEx() & b2) == b2) {
            world.toggleSwitch((int) currentRow, (int) currentCol);
        }

        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        int b2 = MouseEvent.BUTTON2_DOWN_MASK;
        if ((e.getModifiersEx() & b2) == b2) {
            // Mouse wheel drag
            int dx = e.getX() - prevMouseX;
            int dy = -(e.getY() - prevMouseY);
            // compute movement
            x0 += dx;
            y0 += dy;
//            System.out.println(x0 + " " + y0 + " " + zoomLevel);
            repaint();
        } else if (currentTool.equals(GuiTool.TRACK)) {
            currentCol = getCol(e.getX());
            currentRow = getRow(e.getY());
            int currentColInt = (int) currentCol;
            int currentRowInt = (int) currentRow;
            int delta;

            if (ctrlIsPressed && currentDirection != null) {

                // Project the current mouse position onto the initial movement line
                switch (currentDirection) {
                case EAST, WEST:
                    currentRowInt = straightOriginRow;
                    prevRow = straightOriginRow;
                    break;
                case NORTH, SOUTH:
                    currentColInt = straightOriginCol;
                    prevCol = straightOriginCol;
                    break;
                case SOUTHEAST, NORTHWEST:
                    delta = (currentColInt - currentRowInt) - (straightOriginCol - straightOriginRow);
                    if (2 * (delta / 2) != delta) {
                        // delta is odd
                        currentColInt = prevCol;
                        currentRowInt = prevRow;
                    } else {
                        while (delta >= 2) {
                            currentColInt--;
                            currentRowInt++;
                            delta -= 2;
                        }
                        while (delta <= -2) {
                            currentColInt++;
                            currentRowInt--;
                            delta += 2;
                        }
                    }
                    break;
                case NORTHEAST, SOUTHWEST:
                    delta = (currentColInt + currentRowInt) - (straightOriginCol + straightOriginRow);
                    if (2 * (delta / 2) != delta) {
                        // delta is odd
                        currentColInt = prevCol;
                        currentRowInt = prevRow;
                    } else {
                        while (delta >= 2) {
                            currentColInt--;
                            currentRowInt--;
                            delta -= 2;
                        }
                        while (delta <= -2) {
                            currentColInt++;
                            currentRowInt++;
                            delta += 2;
                        }
                    }
                    break;
                }
            }
            // Detect a change in cell
            if (!isInCorner(currentRow, currentCol)) {
                if (prevRow != Integer.MAX_VALUE && prevCol != Integer.MAX_VALUE
                        && (prevCol != currentColInt || prevRow != currentRowInt)) {

                    // Choose or follow a direction when CTRL is pressed.
                    if (ctrlIsPressed && currentDirection == null) {
                        // We decide here how the straight line will be oriented.
                        straightOriginRow = currentRowInt;
                        straightOriginCol = currentColInt;
                        currentDirection = computeMovementDirection(currentRowInt, currentColInt, prevRow, prevCol);
                    }

                    // Each cell develops a link to the other one.
                    world.setNewTrack(prevRow, prevCol, currentRowInt, currentColInt);
                    world.setNewTrack(currentRowInt, currentColInt, prevRow, prevCol);

                    repaint();
                }

                prevCol = currentColInt;
                prevRow = currentRowInt;
            }
        } else if (currentTool.equals(GuiTool.TRACK_REMOVAL)) {
            int currentColInt = (int) currentCol;
            int currentRowInt = (int) currentRow;
            currentCol = getCol(e.getX());
            currentRow = getRow(e.getY());
            world.removeTrack(currentRowInt, currentColInt);
            repaint();
        } else if (currentTool.equals(GuiTool.TRAIN_REMOVAL)) {
            int currentColInt = (int) currentCol;
            int currentRowInt = (int) currentRow;
            currentCol = getCol(e.getX());
            currentRow = getRow(e.getY());
            world.removeTrains(currentRowInt, currentColInt);
            repaint();
        }

        prevMouseX = e.getX();
        prevMouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        prevMouseX = e.getX();
        prevMouseY = e.getY();
        currentCol = getCol(e.getX());
        currentRow = getRow(e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int x = e.getX();
        int y = graphicsCurrentHeight - e.getY();
        double currentZoomLevelFactor;
        if (e.getPreciseWheelRotation() < 0) {
            currentZoomLevelFactor = zoomLevelFactor;
        } else {
            currentZoomLevelFactor = 1 / zoomLevelFactor;
        }
        zoomLevel = zoomLevel * currentZoomLevelFactor;
        x0 = (int) (x - currentZoomLevelFactor * (x - x0));
        y0 = (int) (y - currentZoomLevelFactor * (y - y0));
        repaint();
    }

    /**
     * Compute the column number for the given on-screen x-coordinate
     *
     * @param x the on-screen x-coorrdinate
     * @return the column that contains the given pixel
     */
    private double getCol(int x) {
        double appCellSize = Cell.cellSize * zoomLevel;
        double result = ((double) (x + appCellSize / 2 - x0) / (Cell.cellSize * zoomLevel));
        return result;
    }

    /**
     * Compute the row number for the given on-screen y-coordinate
     *
     * @param y the on-screen y-coordinate
     * @return the row that contains the given pixel
     */
    private double getRow(int y) {
        double appCellSize = Cell.cellSize * zoomLevel;
        double result = world.getNbRows() - (double) ((graphicsCurrentHeight - y) + appCellSize / 2 - y0) / (Cell.cellSize * zoomLevel);
        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        repaint();
    }

    /**
     * Return true when the coordinates correspond to the corner of a cell.
     *
     * @param row
     * @param col
     * @return true when the location defined by row and col is close enough to
     * the side of the cell.
     */
    private boolean isInCorner(double row, double col) {
        double dRow = row - (int) row;
        double dCol = col - (int) col;

        boolean result = (dCol < cornerMargin | dCol > 1 - cornerMargin)
                && (dRow < cornerMargin | dRow > 1 - cornerMargin);
        return result;
    }

    protected void save(FileWriter writer) {
        try {
            writer.write("zoom " + zoomLevel + "\n");
            writer.write("x0 " + x0 + "\n");
            writer.write("y0 " + y0 + "\n");
        } catch (IOException e) {
            System.out.println("WorldPanel: error occured when saving to file.");
        }
    }

    protected void load(Scanner scanner) {
        String text;
        String split[];

        text = scanner.nextLine();
        split = text.split(" ");
        if (split[0].equals("zoom")) {
            zoomLevel = Double.valueOf(split[1]);
        }

        text = scanner.nextLine();
        split = text.split(" ");
        if (split[0].equals("x0")) {
            x0 = Double.valueOf(split[1]);
        }

        text = scanner.nextLine();
        split = text.split(" ");
        if (split[0].equals("y0")) {
            y0 = Double.valueOf(split[1]);
        }
    }

    protected void setControlState(boolean b) {
        ctrlIsPressed = b;
        if (!ctrlIsPressed) {
            currentDirection = null;
        }
    }

    private CardinalPoint computeMovementDirection(int currentRow, int currentCol, int prevRow, int prevCol) {
        if (currentRow < prevRow) {
            if (currentCol < prevCol) {
                // Moving north-west
                return CardinalPoint.NORTHWEST;
            } else if (currentCol == prevCol) {
                // Moving north
                return CardinalPoint.NORTH;
            } else {
                // Moving north-east
                return CardinalPoint.NORTHEAST;
            }
        } else if (currentRow == prevRow) {
            if (currentCol > prevCol) {
                // Moving east
                return CardinalPoint.EAST;
            } else if (currentCol == prevCol) {
                // Not moving
                return CardinalPoint.CENTER;
            } else {
                // Moving west
                return CardinalPoint.WEST;
            }
        } else {
            if (currentCol < prevCol) {
                // Moving south-west
                return CardinalPoint.SOUTHWEST;
            } else if (currentCol == prevCol) {
                // Moving south
                return CardinalPoint.SOUTH;
            } else {
                // Moving south-east
                return CardinalPoint.SOUTHEAST;
            }
        }
    }

    protected String getView() {
        return trimIfLongerThan(x0) + " "
                + trimIfLongerThan(y0) + " "
                + trimIfLongerThan(Math.max(zoomLevel, 0.001));
    }

    protected void setView(ViewPoint v) {
        this.x0 = v.getX();
        this.y0 = v.getY();
        this.zoomLevel = v.getZoom();
    }

    private String trimIfLongerThan(double val) {
        double valAbs = Math.abs(val);
        if (valAbs > 1000) {
            return Math.floor(val) + "";
        } else {
            return (Math.floor(val * 1000)) / 1000 + "";
        }
    }

    protected void toggleDisplayTerrain() {
        mustDisplayTerrain = !mustDisplayTerrain;
        repaint();
    }

    protected void astarRAZ() {
        solver.reset();
        repaint();
    }

    protected void astarStep() {
        solver.step();
        repaint();

    }

    protected int astarFull() {
        long singleStepTimeMs = 100;
        astarThread = new Thread(() -> {
            int result = 0;
            while (result == 0) {
                result = solver.fullSolve(singleStepTimeMs);
                repaint();
            }
        });
        astarThread.start();
        return 0;
    }

    protected void astarToTracks() {
        System.out.println("Converting A* to tracks...");
        ArrayList<PathPoint> path = solver.getFinalPath();
        PathPoint previous = null;
        for (PathPoint current : path) {
            // Link current and previous

            if (previous != null && current != null) {
                int colPrev = previous.x;
                int rowPrev = previous.y;
                int colCurr = current.x;
                int rowCurr = current.y;
                world.setNewTrack(rowPrev, colPrev, rowCurr, colCurr);
                world.setNewTrack(rowCurr, colCurr, rowPrev, colPrev);
            }
            previous = current;
        }
        solver.reset();
        solver.fullReset();
        System.out.println("Converting A* to tracks done.");
    }

    /**
     * Create all the color chart, with one color ramp for each biome.
     * TODO: these values and the biome names must be defined in a file and
     * loaded dynamically.
     */
    private void loadColorRamps() {
        // Two different possible biomes
        allColorRamps = new ArrayList<>();
        allColorRamps.add(new ColorRamp());
        allColorRamps.add(new ColorRamp());
        allColorRamps.add(new ColorRamp());

        ColorRamp ramp0 = allColorRamps.get(0);
        ColorRamp ramp1 = allColorRamps.get(1);
        ColorRamp ramp2 = allColorRamps.get(2);

        double seaLevel = -0.05;
        double delta = 0.001;

        ramp0.addValue(seaLevel - delta, Color.blue.brighter());
        ramp0.addValue(seaLevel, Color.green.brighter());
        ramp0.addValue(0.18, Color.green.darker());
        ramp0.addValue(0.2, Color.gray);
        ramp0.addValue(0.3, Color.white);

        ramp1.addValue(seaLevel - delta, Color.blue.darker());
        ramp1.addValue(seaLevel, Color.green);
        ramp1.addValue(0.18, Color.green.darker().darker());
        ramp1.addValue(0.2, Color.gray.darker());
        ramp1.addValue(0.3, Color.white.darker());

        ramp2.addValue(seaLevel - delta, Color.gray);
        ramp2.addValue(seaLevel, Color.cyan.darker());
        ramp2.addValue(0.18, Color.orange.darker().darker());
        ramp2.addValue(0.2, Color.gray);
        ramp2.addValue(0.3, Color.green.brighter().brighter());
    }

    private Color computeCellColor(double altitude, int biome) {
        return allColorRamps.get(biome).getValue(altitude);
    }
}
