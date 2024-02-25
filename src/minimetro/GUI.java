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

    WorldPanel panel;
    JPanel toolbar;

    int windowWidth = 1100;
    int windowHeight = 1000;

    public GUI(World w) {
        super();

        // Add a layout with a toolbar and a jpanel
        this.setLayout(new BorderLayout());

        toolbar = new JPanel();

        JButton playPauseButton = new JButton("PlayPause");
        playPauseButton.addActionListener((e) -> {
            w.togglePlayPause();
        });
        toolbar.add(playPauseButton);

        JButton stepButton = new JButton("Step");
        stepButton.addActionListener((e) -> {
            w.step();
            repaint();
        });
        toolbar.add(stepButton);

        JButton trackButton = new JButton("Track");
        trackButton.addActionListener((e) -> {
            System.out.println("Pressed track button");
            panel.setTool(GuiTool.TRACK);
        });
        toolbar.add(trackButton);

        JButton stationButton = new JButton("Station");
        stationButton.addActionListener((e) -> {
            System.out.println("Pressed station button");
            panel.setTool(GuiTool.STATION);
        });
        toolbar.add(stationButton);

        JButton locoButton = new JButton("Loco");
        locoButton.addActionListener((e) -> {
            System.out.println("Pressed loco button");
            panel.setTool(GuiTool.LOCO);
        });
        toolbar.add(locoButton);

        JButton zoomInButton = new JButton("Zoom in");
        zoomInButton.addActionListener((e) -> {
            System.out.println("Pressed zoom in button");
            panel.zoomIn();
            panel.repaint();
        });
        toolbar.add(zoomInButton);

        JButton zoomOutButton = new JButton("Zoom out");
        zoomOutButton.addActionListener((e) -> {
            System.out.println("Pressed zoom out button");
            panel.zoomOut();
            panel.repaint();
        });
        toolbar.add(zoomOutButton);

        this.add(toolbar, BorderLayout.SOUTH);

        panel = new WorldPanel(w);
        this.add(panel, BorderLayout.CENTER);

        w.addPropertyChangeListener("currentStep", panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(windowWidth, windowHeight));
        this.setVisible(true);
    }
}
