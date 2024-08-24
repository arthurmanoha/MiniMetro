package minimetro;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author arthu
 */
public class GUI extends JFrame {

    WorldPanel panel;
    JPanel topToolbar;
    JPanel bottomToolbar;
    JPanel mainToolbar;

    int windowWidth = 1900;
    int windowHeight = 1000;

    private World world;
    private JTextField speedIndicatorTextField;

    public GUI(World w) {
        super();

        world = w;

        // Add a layout with a toolbar and a jpanel
        this.setLayout(new BorderLayout());

        mainToolbar = new JPanel();
        mainToolbar.setLayout(new BorderLayout());

        topToolbar = new JPanel();

        JButton playPauseButton = new JButton("PlayPause");
        playPauseButton.addActionListener((e) -> {
            w.togglePlayPause();
        });
        topToolbar.add(playPauseButton);

        JButton stepButton = new JButton("Step");
        stepButton.addActionListener((e) -> {
            w.step();
            repaint();
        });
        topToolbar.add(stepButton);

        JButton trackButton = new JButton("Track");
        trackButton.addActionListener((e) -> {
            System.out.println("Pressed track button");
            panel.setTool(GuiTool.TRACK);
        });
        topToolbar.add(trackButton);

        JButton removeTrackButton = new JButton("Remove Track");
        removeTrackButton.addActionListener((e) -> {
            System.out.println("Pressed remove track button");
            panel.setTool(GuiTool.TRACK_REMOVAL);
        });
        topToolbar.add(removeTrackButton);

        JButton stationButton = new JButton("Station");
        stationButton.addActionListener((e) -> {
            System.out.println("Pressed station button");
            panel.setTool(GuiTool.STATION);
        });
        topToolbar.add(stationButton);

        JButton locoButton = new JButton("Loco");
        locoButton.addActionListener((e) -> {
            System.out.println("Pressed loco button");
            panel.setTool(GuiTool.LOCO);
        });
        topToolbar.add(locoButton);

        JButton wagonButton = new JButton("Wagon");
        wagonButton.addActionListener((e) -> {
            System.out.println("Pressed wagon button");
            panel.setTool(GuiTool.WAGON);
        });
        topToolbar.add(wagonButton);

        JButton removeTrainsButton = new JButton("Remove trains");
        removeTrainsButton.addActionListener((e) -> {
            System.out.println("Pressed remove trains button");
            panel.setTool(GuiTool.TRAIN_REMOVAL);
        });
        topToolbar.add(removeTrainsButton);

        JButton zoomInButton = new JButton("Zoom in");
        zoomInButton.addActionListener((e) -> {
            System.out.println("Pressed zoom in button");
            panel.zoomIn();
            panel.repaint();
        });
        topToolbar.add(zoomInButton);

        JButton zoomOutButton = new JButton("Zoom out");
        zoomOutButton.addActionListener((e) -> {
            System.out.println("Pressed zoom out button");
            panel.zoomOut();
            panel.repaint();
        });
        topToolbar.add(zoomOutButton);

        mainToolbar.add(topToolbar, BorderLayout.NORTH);

        bottomToolbar = new JPanel();

        JButton startLocoButton = new JButton("Start Locos");
        startLocoButton.addActionListener((e) -> {
            w.startLocos();
        });
        bottomToolbar.add(startLocoButton);

        JButton stopLocoButton = new JButton("Stop Locos");
        stopLocoButton.addActionListener((e) -> {
            w.stopLocos();
        });
        bottomToolbar.add(stopLocoButton);

        speedIndicatorTextField = new JTextField();
        speedIndicatorTextField.setPreferredSize(new Dimension(80, 27));
        speedIndicatorTextField.setText("0.0");
        speedIndicatorTextField.addActionListener((e) -> {
            readSpeedLimit();
        });
        bottomToolbar.add(speedIndicatorTextField);

        JButton speedCellButton = new JButton("Apply Speed Indicator");
        speedCellButton.addActionListener((e) -> {
            readSpeedLimit();
            panel.setTool(GuiTool.SPEED_INDICATOR);
        });
        bottomToolbar.add(speedCellButton);

        mainToolbar.add(bottomToolbar, BorderLayout.SOUTH);

        this.add(mainToolbar, BorderLayout.SOUTH);

        panel = new WorldPanel(w);
        this.add(panel, BorderLayout.CENTER);

        // Speed limit: positive value
        JPanel speedToolbar = new JPanel();
        speedToolbar.add(new JLabel("Speed limits: "));
        JButton limit01Button = new JButton("0.1");
        limit01Button.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            w.setSpeedLimitValue(0.1);
        });
        speedToolbar.add(limit01Button);
        JButton limit5Button = new JButton("5");
        limit5Button.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            w.setSpeedLimitValue(5);
        });
        speedToolbar.add(limit5Button);
        JButton limit10Button = new JButton("10");
        limit10Button.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            w.setSpeedLimitValue(10);
        });
        speedToolbar.add(limit10Button);

        // End of limit: value -1
        JButton endOfLimitButton = new JButton("End of limit");
        endOfLimitButton.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            w.setSpeedLimitValue(-1);
        });
        speedToolbar.add(endOfLimitButton);

        // No limit information in this cell: value Integer.MAX_VALUE
        JButton noLimitButton = new JButton("No limit");
        noLimitButton.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            w.setSpeedLimitValue(Integer.MAX_VALUE);
        });
        speedToolbar.add(noLimitButton);

        this.add(speedToolbar, BorderLayout.NORTH);

        w.addPropertyChangeListener("currentStep", panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(windowWidth, windowHeight));
        this.setVisible(true);
    }

    private void readSpeedLimit() {
        double value = Double.valueOf(speedIndicatorTextField.getText());
        System.out.println("Value read from text field: " + value);
        world.setSpeedLimitValue(value);
    }
}
