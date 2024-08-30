package minimetro;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 *
 * @author arthu
 */
public class GUI extends JFrame {

    WorldPanel panel;
    JPanel topToolbar;
    JPanel locoToolbar;
    JPanel speedToolbar;
    JPanel mainToolbar;

    int windowWidth = 1000;
    int windowHeight = 1000;

    private World world;
    private JTextField speedIndicatorTextField;
    private JTextField stopTimerTextField;
    double defaultStopTimerDuration = 5;

    public GUI(World w) {
        super();

        world = w;

        // Add a layout with a toolbar and a jpanel
        this.setLayout(new BorderLayout());

        mainToolbar = new JPanel();
        mainToolbar.setLayout(new GridLayout(3, 1));

        topToolbar = new JPanel();
        topToolbar.setLayout(new GridLayout(14, 1));

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
            panel.setTool(GuiTool.TRACK_REMOVAL);
        });
        topToolbar.add(removeTrackButton);

        JButton stationButton = new JButton("Station");
        stationButton.addActionListener((e) -> {
            panel.setTool(GuiTool.STATION);
        });
        topToolbar.add(stationButton);

        JButton locoButton = new JButton("Loco");
        locoButton.addActionListener((e) -> {
            panel.setTool(GuiTool.LOCO);
        });
        topToolbar.add(locoButton);

        JButton wagonButton = new JButton("Wagon");
        wagonButton.addActionListener((e) -> {
            panel.setTool(GuiTool.WAGON);
        });
        topToolbar.add(wagonButton);

        JButton removeTrainsButton = new JButton("Remove trains");
        removeTrainsButton.addActionListener((e) -> {
            panel.setTool(GuiTool.TRAIN_REMOVAL);
        });
        topToolbar.add(removeTrainsButton);

        JButton zoomInButton = new JButton("Zoom in");
        zoomInButton.addActionListener((e) -> {
            panel.zoomIn();
            panel.repaint();
        });
        topToolbar.add(zoomInButton);

        JButton zoomOutButton = new JButton("Zoom out");
        zoomOutButton.addActionListener((e) -> {
            panel.zoomOut();
            panel.repaint();
        });
        topToolbar.add(zoomOutButton);

        JButton boardButton = new JButton("Board");
        boardButton.addActionListener((e) -> {
            w.boardPassengers();
        });
        topToolbar.add(boardButton);

        JButton getOffButton = new JButton("Get off");
        getOffButton.addActionListener((e) -> {
            w.getPassengersOff();
        });
        topToolbar.add(getOffButton);

        JButton generatePassengersButton = new JButton("Generate Passengers");
        generatePassengersButton.addActionListener((e) -> {
            w.generatePassengers();
        });
        topToolbar.add(generatePassengersButton);

        mainToolbar.add(topToolbar);

        locoToolbar = new JPanel();
        locoToolbar.setLayout(new GridLayout(4, 2));

        JButton startLocoButton = new JButton("Start Locos");
        startLocoButton.addActionListener((e) -> {
            w.startLocos();
        });
        locoToolbar.add(startLocoButton);

        JButton stopLocoButton = new JButton("Stop Locos");
        stopLocoButton.addActionListener((e) -> {
            w.stopLocos();
        });
        locoToolbar.add(stopLocoButton);

        mainToolbar.add(locoToolbar);

        JButton worldMapButton = new JButton("Display World Map");
        locoToolbar.add(worldMapButton);

        JButton clearMapButton = new JButton("Clear Map");
        clearMapButton.addActionListener((e) -> {
            w.clearMap();
        });

        locoToolbar.add(clearMapButton);
        JLabel worldMapLabel = new JLabel();
        worldMapButton.addActionListener((e) -> {
            worldMapLabel.setText(World.map.toFormattedString());
        });
        JScrollPane scrollMapLabel = new JScrollPane(worldMapLabel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        locoToolbar.add(scrollMapLabel);

        this.add(mainToolbar, BorderLayout.WEST);

        panel = new WorldPanel(w);
        this.add(panel, BorderLayout.CENTER);

        // Speed limit: positive value
        speedToolbar = new JPanel();
        speedToolbar.setLayout(new GridLayout(11, 1));
        speedToolbar.add(new JLabel("Speed limits: "));
        JButton limit01Button = new JButton("0.1");
        limit01Button.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            w.setSpeedLimitValue(0.1);
        });
        speedToolbar.add(limit01Button);
        JButton limit2Button = new JButton("2");
        limit2Button.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            w.setSpeedLimitValue(2);
        });
        speedToolbar.add(limit2Button);
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

        speedIndicatorTextField = new JTextField();
        speedIndicatorTextField.setPreferredSize(new Dimension(80, 27));
        speedIndicatorTextField.setText("0.0");
        speedIndicatorTextField.addActionListener((e) -> {
            readSpeedLimit();
            panel.setTool(GuiTool.SPEED_INDICATOR);
        });
        speedToolbar.add(speedIndicatorTextField);

        JButton speedCellButton = new JButton("Custom speed limit");
        speedCellButton.addActionListener((e) -> {
            readSpeedLimit();
            panel.setTool(GuiTool.SPEED_INDICATOR);
        });
        speedToolbar.add(speedCellButton);

        stopTimerTextField = new JTextField();
        stopTimerTextField.setPreferredSize(new Dimension(80, 27));
        stopTimerTextField.setText("" + defaultStopTimerDuration);
        stopTimerTextField.addActionListener((e) -> {
            readStopTimer();
        });
        speedToolbar.add(stopTimerTextField);

        JButton stopTimerButton = new JButton("Stop Timer");
        stopTimerButton.addActionListener((e) -> {
            readStopTimer();
            panel.setTool(GuiTool.STOP_TIMER);
        });
        speedToolbar.add(stopTimerButton);

        mainToolbar.add(speedToolbar);

        w.addPropertyChangeListener("currentStep", panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(windowWidth, windowHeight));
        this.setVisible(true);
    }

    private void readSpeedLimit() {
        try {
            double value = Double.valueOf(speedIndicatorTextField.getText());
            System.out.println("Value read from text field: " + value);
            world.setSpeedLimitValue(value);
        } catch (NumberFormatException e) {
            System.out.println("Value incorrect, speed limit unchanged.");
        }
    }

    private void readStopTimer() {
        try {
            double value = Double.valueOf(stopTimerTextField.getText());
            System.out.println("Value read from text field: " + value);
            world.setStopTimerDuration(value);
        } catch (NumberFormatException e) {
            System.out.println("Value incorrect, stop timer unchanged.");
        }
    }
}
