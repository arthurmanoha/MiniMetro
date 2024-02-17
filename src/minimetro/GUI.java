package minimetro;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author arthu
 */
public class GUI extends JFrame {

    JPanel panel;
    JPanel toolbar;

    public GUI(World w) {
        super();

        // Add a layout with a toolbar and a jpanel
        this.setLayout(new BorderLayout());

        toolbar = new JPanel();

        JButton playPauseButton = new JButton("PlayPause");
        toolbar.add(playPauseButton);
        JButton trackButton = new JButton("Track");
        toolbar.add(trackButton);

        this.add(toolbar, BorderLayout.SOUTH);

        panel = new WorldPanel(w);
        this.add(panel, BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(800, 600));
        this.setVisible(true);
    }
}
