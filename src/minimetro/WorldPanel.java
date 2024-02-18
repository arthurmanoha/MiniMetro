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

    public WorldPanel(World w) {
        super();
        setSize(new Dimension(800, 600));
        world = w;
        zoomLevel = 40;
        zoomLevelFactor = 1.1;
        x0 = 0;
        y0 = 0;
        currentTool = GuiTool.NO_TOOL;
        prevMouseX = 0;
        prevMouseY = 0;

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
            Cell c = this.getCell(e.getX(), e.getY());
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
     * find and return the cell that contains the pixel at coordinates (x, y);
     *
     * @param x x-coordinate of the target pixel
     * @param y y-coordinate of the target pixel
     * @return the cell that contains tha pixel, null if the pixel is outside
     * all cells.
     */
    private Cell getCell(int x, int y) {

        return null;
    }
}
