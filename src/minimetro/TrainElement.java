package minimetro;

import java.awt.Graphics;

/**
 * This class represents either a locomotive or a passenger carriage.
 *
 * @author arthu
 */
public abstract class TrainElement {

    public abstract void paint(Graphics g, int xApp, int yApp, int size);
}
