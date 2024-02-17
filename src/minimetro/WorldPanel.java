package minimetro;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author arthu
 */
public class WorldPanel extends JPanel {

    World world;

    public WorldPanel(World w) {
        super();
        setSize(new Dimension(800, 600));
        world = w;
    }

    @Override
    public void paintComponent(Graphics g) {

        for (int row = 0; row < world.getNbRows(); row++) {
            for (int col = 0; col < world.getNbCols(); col++) {
                Cell c = world.getCell(row, col);
                c.paint(g, row, col);
            }
        }
    }
}
