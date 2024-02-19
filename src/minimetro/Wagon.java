package minimetro;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author arthu
 */
public class Wagon extends TrainElement {

    public Wagon() {
    }

    @Override
    public void paint(Graphics g, int xApp, int yApp, int size) {
        System.out.println("Wagon paint, x = " + xApp + ", y = " + yApp);
        g.setColor(Color.blue);
        g.fillOval(xApp, yApp, size, size);
        g.setColor(Color.black);
        g.drawOval(xApp, yApp, size, size);
    }
}
