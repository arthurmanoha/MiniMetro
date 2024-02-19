package minimetro;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author arthu
 */
public class Locomotive extends TrainElement {

    public Locomotive() {
    }

    @Override
    public void paint(Graphics g, int xApp, int yApp, int size) {
        System.out.println("Loco paint, x = " + xApp + ", y = " + yApp);
        g.setColor(Color.red);
        g.fillOval(xApp, yApp, size, size);
        g.setColor(Color.black);
        g.drawOval(xApp, yApp, size, size);
    }
}
