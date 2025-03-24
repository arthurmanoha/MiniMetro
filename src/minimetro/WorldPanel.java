package minimetro;

import colorramp.ColorRamp;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
import java.util.Scanner;
import javax.swing.JPanel;

/**
 *
 * @author arthu
 */
public class WorldPanel extends JPanel implements MouseListener,
        MouseMotionListener, MouseWheelListener, PropertyChangeListener {

    private static boolean DISPLAY_ACTIVE_CELLS_BORDERS = true;

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

    private ColorRamp colorRamp;
    private boolean mustDisplayTerrain;

    private Color defaultBackgroundColor;

    private AStarSolver solver;

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

        colorRamp = new ColorRamp();

        double seaLevel = -0.05;
        double delta = 0.001;
        colorRamp.addValue(seaLevel - delta, Color.blue);
        colorRamp.addValue(seaLevel, Color.green);
        colorRamp.addValue(0.18, Color.green.darker());
        colorRamp.addValue(0.2, Color.gray);
        colorRamp.addValue(0.3, Color.white);
        defaultBackgroundColor = Color.gray;

        mustDisplayTerrain = true;

        solver = new AStarSolver(world);
    }

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
                Color color = colorRamp.getValue(c.altitude);
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

    protected void astarFull() {
        solver.fullSolve();
    }
}
