package minimetro;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 *
 * @author arthu
 */
public class CustomKeyListener extends KeyAdapter {

    private GUI gui;

    public CustomKeyListener(GUI newGui) {
        this.gui = newGui;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            gui.setControlState(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            gui.setControlState(false);
        }
    }
}
