package minimetro;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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

    int windowWidth = 1100;
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
        topToolbar.setLayout(new GridLayout(17, 1));

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

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener((e) -> {
            save();
        });
        topToolbar.add(saveButton);

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener((e) -> {
            load();
        });
        topToolbar.add(loadButton);

        mainToolbar.add(topToolbar);

        locoToolbar = new JPanel();
        locoToolbar.setLayout(new GridBagLayout());

        JButton startLocoButton = new JButton("Start Locos");
        startLocoButton.addActionListener((e) -> {
            w.startLocos();
        });
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        locoToolbar.add(startLocoButton, c);

        JButton stopLocoButton = new JButton("Stop Locos");
        stopLocoButton.addActionListener((e) -> {
            w.stopLocos();
        });
        c.gridx = 1;
        c.gridy = 0;
        locoToolbar.add(stopLocoButton, c);

        JButton worldMapButton = new JButton("Display World Map");
        c.gridx = 0;
        c.gridy = 1;
        locoToolbar.add(worldMapButton, c);

        JLabel worldMapLabel = new JLabel();
        worldMapButton.addActionListener((e) -> {
            World.map.computeWalkways();
            worldMapLabel.setText(World.map.toFormattedString());
        });
        JScrollPane mapScrollPane = new JScrollPane(worldMapLabel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        mapScrollPane.setPreferredSize(new Dimension(200, 250));
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        locoToolbar.add(mapScrollPane, c);

        JButton clearMapButton = new JButton("Clear Map");
        clearMapButton.addActionListener((e) -> {
            w.clearMap();
        });
        c.gridx = 1;
        c.gridy = 1;
        locoToolbar.add(clearMapButton, c);

        mainToolbar.add(locoToolbar);

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

    protected void save() {
        System.out.println("Saving to file");

        // Read the config file to know where to look
        File configFile = new File("config.txt");
        try {
            Scanner scanner = new Scanner(configFile);
            String text = "";
            while (scanner.hasNextLine()) {
                text = scanner.nextLine();
            }
            JFileChooser fileChooser = new JFileChooser(text);
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                world.save(file);
            }
        } catch (FileNotFoundException ex) {
            // No config file, maybe create it here.
            System.out.println("Cannot save, no config file.");
        }
    }

    protected void load() {

        // Read the config file to know where to look
        File configFile = new File("config.txt");
        try {
            Scanner scanner = new Scanner(configFile);
            String text = "";
            while (scanner.hasNextLine()) {
                text = scanner.nextLine();
            }
            JFileChooser fileChooser = new JFileChooser(text);
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                world.load(file);
            }
        } catch (FileNotFoundException ex) {
            // No config file, maybe create it here.
            System.out.println("Cannot load, no config file.");
        }
    }
}
