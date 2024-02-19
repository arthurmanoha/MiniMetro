package minimetro;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPanel;

/**
 *
 * @author arthu
 */
public class WorldPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    World world;
    private double zoomLevel;
    private double zoomLevelFactor;
    private int x0, y0;
    GuiTool currentTool;
    private int prevMouseX, prevMouseY;

    // Memorize the current cell and the last two, necessary to create rails.
    private int prevPrevCol, prevPrevRow;
    private int prevCol, prevRow;
    private int currentCol, currentRow;

    public WorldPanel(World w) {
        super();
        setSize(new Dimension(800, 600));
        world = w;
        zoomLevel = 60;
        zoomLevelFactor = 1.1;
        x0 = 0;
        y0 = 0;
        currentTool = GuiTool.NO_TOOL;
        prevMouseX = 0;
        prevMouseY = 0;
        prevPrevRow = Integer.MAX_VALUE;
        prevPrevCol = Integer.MAX_VALUE;
        prevRow = Integer.MAX_VALUE;
        prevCol = Integer.MAX_VALUE;

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {

        g.setColor(Color.gray);
        g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);

        for (int row = 0; row < world.getNbRows(); row++) {
            for (int col = 0; col < world.getNbCols(); col++) {
                Cell c = world.getCell(row, col);
                c.paint(g, row, col, x0, y0, zoomLevel);
            }
        }
    }

    void setTool(GuiTool newTool) {
        currentTool = newTool;
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
        if ((e.getModifiersEx() & b1) == b1) {
            // Clicked first mouse button
            switch (currentTool) {
            case LOCO -> {
                currentCol = getCol(e.getX());
                currentRow = getRow(e.getY());
                world.addLoco(currentRow, currentCol);
            }
            case WAGON -> {
                currentCol = getCol(e.getX());
                currentRow = getRow(e.getY());
                world.addWagon(currentRow, currentCol);
            }
            case TRACK -> {
                prevPrevCol = Integer.MAX_VALUE;
                prevPrevRow = Integer.MAX_VALUE;
                prevCol = Integer.MAX_VALUE;
                prevRow = Integer.MAX_VALUE;
                currentCol = getCol(e.getX());
                currentRow = getRow(e.getY());
            }
            case STATION -> {
                System.out.println("New station created");
                int col = getCol(e.getX());
                int row = getRow(e.getY());
                world.toggleStation(row, col);
                repaint();
            }
            default -> {
            }
            }
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
            int dy = e.getY() - prevMouseY;
            // compute movement
            x0 += dx;
            y0 += dy;

            prevMouseX = e.getX();
            prevMouseY = e.getY();
            repaint();
        } else if (currentTool.equals(GuiTool.TRACK)) {
            int col = getCol(e.getX());
            int row = getRow(e.getY());

            // Detect a change in cell
            if (col != currentCol || row != currentRow) {
                if (prevCol != Integer.MAX_VALUE) {
                    prevPrevCol = prevCol;
                }
                prevCol = currentCol;
                currentCol = col;

                if (prevRow != Integer.MAX_VALUE) {
                    prevPrevRow = prevRow;
                }
                prevRow = currentRow;
                currentRow = row;

                if (draggedAcrossThreeCells()) {
                    world.setNewTrack(prevPrevCol, prevPrevRow, prevCol, prevRow, currentCol, currentRow);
                    repaint();
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        prevMouseX = e.getX();
        prevMouseY = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int x = e.getX();
        int y = e.getY();
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
    private int getCol(int x) {
        return (int) ((x - x0) / zoomLevel);
    }

    /**
     * Compute the row number for the given on-screen y-coordinate
     *
     * @param y the on-screen y-coorrdinate
     * @return the row that contains the given pixel
     */
    private int getRow(int y) {
        return (int) ((y - y0) / zoomLevel);
    }

    /**
     * Compare prevPrev, prev and current rows and columns,
     * to check if we have dragges the mouse across three successive neighbor
     * cells.
     *
     * @return true if three neighbor cells have been dragged across.
     */
    private boolean draggedAcrossThreeCells() {

        if (prevPrevCol == Integer.MAX_VALUE || prevPrevRow == Integer.MAX_VALUE) {
            // We only have dragged across two cells.
            return false;
        }

        // If any of these 3 conditions is met, we have either not moved or returned to the starting cell.
        boolean prevEqualsPrevPrev = (prevPrevCol == prevCol && prevPrevRow == prevRow);
        boolean prevEqualsCurrent = (prevCol == currentCol && prevRow == currentRow);
        boolean prevPrevEqualsCurrent = (prevPrevCol == currentCol && prevPrevRow == currentRow);

        if (prevEqualsPrevPrev) {
            return false;
        } else if (prevEqualsCurrent) {
            return false;
        } else if (prevPrevEqualsCurrent) {
            return false;
        } else {
            return true;
        }
    }

}
