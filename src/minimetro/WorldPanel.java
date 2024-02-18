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
    private int prevCol, prevRow;

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
        prevRow = 0;
        prevCol = 0;

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
        } else if (currentTool.equals(GuiTool.STATION)) {
            int col = getCol(e.getX());
            int row = getRow(e.getY());
            world.setCell(row, col, new StationCell());
            repaint();
        } else if (currentTool.equals(GuiTool.TRACK)) {
            int col = getCol(e.getX());
            int row = getRow(e.getY());
            if (prevCol != col || prevRow != row) {
                // We have dragged the mouse from one cell to another,
                // They need to be linked with a track.
                world.setTrack(row, col, prevRow, prevCol);
                repaint();
            }
            prevCol = col;
            prevRow = row;
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
}
