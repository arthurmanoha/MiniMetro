package minimetro;

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
import javax.swing.JPanel;

/**
 *
 * @author arthu
 */
public class WorldPanel extends JPanel implements MouseListener,
        MouseMotionListener, MouseWheelListener, PropertyChangeListener {

    World world;
    private double zoomLevel;
    private double zoomLevelFactor;
    private double x0, y0;
    GuiTool currentTool;
    private int prevMouseX, prevMouseY;

    // Memorize the current cell and the last two, necessary to create rails.
    private int prevCol, prevRow;
    private int currentCol, currentRow;

    private int graphicsCurrentHeight;

    public WorldPanel(World w) {
        super();
        setSize(new Dimension(800, 600));
        world = w;
        zoomLevel = 259.37;
        zoomLevelFactor = 1.1;
        x0 = -78;
        y0 = -1411;
        currentTool = GuiTool.NO_TOOL;
        prevMouseX = 0;
        prevMouseY = 0;
        prevRow = Integer.MAX_VALUE;
        prevCol = Integer.MAX_VALUE;

        graphicsCurrentHeight = 0;

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {

        int gWidth = g.getClipBounds().width;
        graphicsCurrentHeight = g.getClipBounds().height;

        // Erase the whole panel
        g.setColor(Color.gray);
        g.fillRect(0, 0, gWidth, graphicsCurrentHeight);

        // Paint the background
        for (int row = 0; row < world.getNbRows(); row++) {
            for (int col = 0; col < world.getNbCols(); col++) {
                Cell c = world.getCell(row, col);
                c.paint(g, x0, y0, zoomLevel);
            }
        }

        // Paint the train links
        paintTrainLinks(g, x0, y0, zoomLevel);

        // Paint the trains
        for (int row = 0; row < world.getNbRows(); row++) {
            for (int col = 0; col < world.getNbCols(); col++) {
                Cell c = world.getCell(row, col);
                c.paintTrains(g, x0, y0, zoomLevel);
            }
        }

        // Draw outer border
        g.setColor(Color.orange);
        double appCellSize = Cell.cellSize * zoomLevel;
        g.drawRect(
                (int) (x0 - appCellSize / 2),
                (int) (graphicsCurrentHeight - (y0 + world.getNbRows() * appCellSize - appCellSize / 2)),
                (int) (world.getNbCols() * zoomLevel),
                (int) (world.getNbRows() * zoomLevel));
    }

    void paintTrainLinks(Graphics g, double x0, double y0, double zoom) {
        for (TrainLink link : world.links) {
            link.paint(g, x0, y0, zoom);
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

            double xReal = (e.getX() - x0) / zoomLevel;
            double yReal = (graphicsCurrentHeight - e.getY() - y0) / zoomLevel;

            // Clicked first mouse button
            switch (currentTool) {
            case LOCO -> {
                world.addLoco(xReal, yReal);
            }
            case WAGON -> {
                world.addWagon(xReal, yReal);
            }
            case TRAIN_REMOVAL -> {
                currentCol = getCol(e.getX());
                currentRow = getRow(e.getY());
                world.removeTrains(currentRow, currentCol);
                repaint();
            }
            case TRACK -> {
                prevCol = Integer.MAX_VALUE;
                prevRow = Integer.MAX_VALUE;
            }
            case STATION -> {
                world.toggleStation(currentRow, currentCol);
                repaint();
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
            int dy = -(e.getY() - prevMouseY);
            // compute movement
            x0 += dx;
            y0 += dy;

            repaint();
        } else if (currentTool.equals(GuiTool.TRACK)) {
            currentCol = getCol(e.getX());
            currentRow = getRow(e.getY());

            // Detect a change in cell
            if (prevRow != Integer.MAX_VALUE && prevCol != Integer.MAX_VALUE
                    && (prevCol != currentCol || prevRow != currentRow)) {

                // Each cell develops a link to the other one.
                world.setNewTrack(prevRow, prevCol, currentRow, currentCol);
                world.setNewTrack(currentRow, currentCol, prevRow, prevCol);

                repaint();
            }

            prevCol = currentCol;
            prevRow = currentRow;
        } else if (currentTool.equals(GuiTool.TRACK_REMOVAL)) {
            currentCol = getCol(e.getX());
            currentRow = getRow(e.getY());
            world.removeTrack(currentRow, currentCol);
            repaint();
        } else if (currentTool.equals(GuiTool.TRAIN_REMOVAL)) {
            currentCol = getCol(e.getX());
            currentRow = getRow(e.getY());
            world.removeTrains(currentRow, currentCol);
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
    private int getCol(int x) {
        double appCellSize = Cell.cellSize * zoomLevel;
        int result = (int) ((double) (x + appCellSize / 2 - x0) / zoomLevel);
        return result;
    }

    /**
     * Compute the row number for the given on-screen y-coordinate
     *
     * @param y the on-screen y-coorrdinate
     * @return the row that contains the given pixel
     */
    private int getRow(int y) {
        double appCellSize = Cell.cellSize * zoomLevel;
        int result = world.getNbRows() - (int) ((double) ((graphicsCurrentHeight - y) + appCellSize / 2 - y0) / zoomLevel) - 1;
        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        /* Récupère l'objet source */
        repaint();
    }
}
