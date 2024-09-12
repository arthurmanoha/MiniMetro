package minimetro;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
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
    JLabel worldMapLabel;

    int windowWidth = 1100;
    int windowHeight = 1000;

    private World world;
    private JTextField speedIndicatorTextField;
    private JTextField stopTimerTextField;
    double defaultStopTimerDuration = 5;

    private CustomKeyListener keyListener;
    private ArrayList<Component> allComponents;

    public GUI(World newWorld) {
        super();

        world = newWorld;
        allComponents = new ArrayList<>();

        keyListener = new CustomKeyListener(this);
        setLayoutAndButtons();
        this.addKeyListener(keyListener);
        allComponents.add(panel);
        allComponents.add(topToolbar);
        allComponents.add(locoToolbar);
        allComponents.add(speedToolbar);
    }

    private void setLayoutAndButtons() {

        allComponents = new ArrayList<>();
        // Add a layout with a toolbar and a jpanel
        this.setLayout(new BorderLayout());

        mainToolbar = new JPanel();
        allComponents.add(mainToolbar);
        mainToolbar.setLayout(new GridLayout(3, 1));

        topToolbar = new JPanel();
        allComponents.add(topToolbar);
        topToolbar.setLayout(new GridLayout(17, 1));

        JButton playPauseButton = new JButton("PlayPause");
        allComponents.add(playPauseButton);
        playPauseButton.addActionListener((e) -> {
            world.togglePlayPause();
        });
        topToolbar.add(playPauseButton);

        JButton stepButton = new JButton("Step");
        allComponents.add(stepButton);
        stepButton.addActionListener((e) -> {
            world.step();
            repaint();
        });
        topToolbar.add(stepButton);

        JButton trackButton = new JButton("Track");
        allComponents.add(trackButton);
        trackButton.addActionListener((e) -> {
            System.out.println("Pressed track button");
            panel.setTool(GuiTool.TRACK);
        });
        topToolbar.add(trackButton);

        JButton removeTrackButton = new JButton("Remove Track");
        allComponents.add(removeTrackButton);
        removeTrackButton.addActionListener((e) -> {
            panel.setTool(GuiTool.TRACK_REMOVAL);
        });
        topToolbar.add(removeTrackButton);

        JButton stationButton = new JButton("Station");
        allComponents.add(stationButton);
        stationButton.addActionListener((e) -> {
            panel.setTool(GuiTool.STATION);
        });
        topToolbar.add(stationButton);

        JButton locoButton = new JButton("Loco");
        allComponents.add(locoButton);
        locoButton.addActionListener((e) -> {
            panel.setTool(GuiTool.LOCO);
        });
        topToolbar.add(locoButton);

        JButton wagonButton = new JButton("Wagon");
        allComponents.add(wagonButton);
        wagonButton.addActionListener((e) -> {
            panel.setTool(GuiTool.WAGON);
        });
        topToolbar.add(wagonButton);

        JButton removeTrainsButton = new JButton("Remove trains");
        allComponents.add(removeTrainsButton);
        removeTrainsButton.addActionListener((e) -> {
            panel.setTool(GuiTool.TRAIN_REMOVAL);
        });
        topToolbar.add(removeTrainsButton);

        JButton zoomInButton = new JButton("Zoom in");
        allComponents.add(zoomInButton);
        zoomInButton.addActionListener((e) -> {
            panel.zoomIn();
            panel.repaint();
        });
        topToolbar.add(zoomInButton);

        JButton zoomOutButton = new JButton("Zoom out");
        allComponents.add(zoomOutButton);
        zoomOutButton.addActionListener((e) -> {
            panel.zoomOut();
            panel.repaint();
        });
        topToolbar.add(zoomOutButton);

        JButton boardButton = new JButton("Board");
        allComponents.add(boardButton);
        boardButton.addActionListener((e) -> {
            world.boardPassengers();
        });
        topToolbar.add(boardButton);

        JButton getOffButton = new JButton("Get off");
        allComponents.add(getOffButton);
        getOffButton.addActionListener((e) -> {
            world.getPassengersOff();
        });
        topToolbar.add(getOffButton);

        JButton generatePassengersButton = new JButton("Generate Passengers");
        allComponents.add(generatePassengersButton);
        generatePassengersButton.addActionListener((e) -> {
            world.generatePassengers();
            panel.repaint();
        });
        topToolbar.add(generatePassengersButton);
        topToolbar.add(getOffButton);

        JButton removePassengersButton = new JButton("Remove Passengers");
        allComponents.add(removePassengersButton);
        removePassengersButton.addActionListener((e) -> {
            world.removePassengers();
            panel.repaint();
        });
        topToolbar.add(removePassengersButton);

        JButton saveButton = new JButton("Save");
        allComponents.add(saveButton);
        saveButton.addActionListener((e) -> {
            save();
        });
        topToolbar.add(saveButton);

        JButton loadButton = new JButton("Load");
        allComponents.add(loadButton);
        loadButton.addActionListener((e) -> {
            load();
        });
        topToolbar.add(loadButton);

        mainToolbar.add(topToolbar);

        locoToolbar = new JPanel();
        allComponents.add(locoToolbar);
        locoToolbar.setLayout(new GridBagLayout());

        JButton startLocoButton = new JButton("Start Locos");
        allComponents.add(startLocoButton);
        startLocoButton.addActionListener((e) -> {
            world.startLocos();
        });
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        locoToolbar.add(startLocoButton, c);

        JButton stopLocoButton = new JButton("Stop Locos");
        allComponents.add(stopLocoButton);
        stopLocoButton.addActionListener((e) -> {
            world.stopLocos();
        });
        c.gridx = 1;
        c.gridy = 0;
        locoToolbar.add(stopLocoButton, c);

        JButton worldMapButton = new JButton("Display World Map");
        allComponents.add(worldMapButton);
        c.gridx = 0;
        c.gridy = 1;
        locoToolbar.add(worldMapButton, c);

        worldMapLabel = new JLabel();
        allComponents.add(worldMapLabel);
        worldMapButton.addActionListener((e) -> {
            World.map.computeWalkways();
            worldMapLabel.setText(World.map.toFormattedString());
            System.out.println("Map: " + World.map.toFormattedString());
        });
        JScrollPane mapScrollPane = new JScrollPane(worldMapLabel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        allComponents.add(mapScrollPane);

        mapScrollPane.setPreferredSize(new Dimension(200, 250));
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        locoToolbar.add(mapScrollPane, c);

        JButton clearMapButton = new JButton("Clear Map");
        allComponents.add(clearMapButton);
        clearMapButton.addActionListener((e) -> {
            world.clearMap();
            worldMapLabel.setText(World.map.toFormattedString());
        });
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 1;
        locoToolbar.add(clearMapButton, c);

        JLabel startStationIdLabel = new JLabel("Start station");
        allComponents.add(startStationIdLabel);
        c.gridx = 0;
        c.gridy = 3;
        startStationIdLabel.setMinimumSize(new Dimension(100, 20));
        locoToolbar.add(startStationIdLabel, c);

        JTextField startStationIdTextField = new JTextField("0");
        allComponents.add(startStationIdTextField);
        c.gridx = 0;
        c.gridy = 4;
        startStationIdTextField.setMinimumSize(new Dimension(100, 20));
        locoToolbar.add(startStationIdTextField, c);

        JLabel endStationIdLabel = new JLabel("End station");
        allComponents.add(endStationIdLabel);
        c.gridx = 1;
        c.gridy = 3;
        endStationIdLabel.setMinimumSize(new Dimension(100, 20));
        locoToolbar.add(endStationIdLabel, c);

        JTextField endStationIdTextField = new JTextField("0");
        allComponents.add(endStationIdTextField);
        c.gridx = 1;
        c.gridy = 4;
        endStationIdTextField.setMinimumSize(new Dimension(100, 20));
        locoToolbar.add(endStationIdTextField, c);

        JLabel nbPassengersLabel = new JLabel("Nb passengers");
        allComponents.add(nbPassengersLabel);
        c.gridx = 2;
        c.gridy = 3;
        nbPassengersLabel.setMinimumSize(new Dimension(100, 20));
        locoToolbar.add(nbPassengersLabel, c);

        JTextField nbPassengersTextField = new JTextField("1");
        allComponents.add(nbPassengersTextField);
        c.gridy = 4;
        nbPassengersTextField.setMinimumSize(new Dimension(100, 20));
        locoToolbar.add(nbPassengersTextField, c);

        JButton createPassengersButton = new JButton("Go");
        allComponents.add(createPassengersButton);
        createPassengersButton.addActionListener((e) -> {
            int startStationId = Integer.valueOf(startStationIdTextField.getText());
            int endStationId = Integer.valueOf(endStationIdTextField.getText());
            int nbPassengers = Integer.valueOf(nbPassengersTextField.getText());
            world.createPassengers(nbPassengers, startStationId, endStationId);
            repaint();
        });
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 3;
        createPassengersButton.setMinimumSize(new Dimension(150, 20));
        locoToolbar.add(createPassengersButton, c);

        mainToolbar.add(locoToolbar);

        this.add(mainToolbar, BorderLayout.WEST);

        panel = new WorldPanel(world);

        this.add(panel, BorderLayout.CENTER);

        // Speed limit: positive value
        speedToolbar = new JPanel();
        speedToolbar.setFocusable(true);
        speedToolbar.setLayout(new GridLayout(11, 1));
        speedToolbar.add(new JLabel("Speed limits: "));
        JButton limit01Button = new JButton("0.1");
        allComponents.add(limit01Button);
        limit01Button.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            world.setSpeedLimitValue(0.1);
        });
        speedToolbar.add(limit01Button);
        JButton limit2Button = new JButton("2");
        allComponents.add(limit2Button);
        limit2Button.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            world.setSpeedLimitValue(2);
        });
        speedToolbar.add(limit2Button);
        JButton limit5Button = new JButton("5");
        allComponents.add(limit5Button);
        limit5Button.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            world.setSpeedLimitValue(5);
        });
        speedToolbar.add(limit5Button);
        JButton limit10Button = new JButton("10");
        allComponents.add(limit10Button);
        limit10Button.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            world.setSpeedLimitValue(10);
        });
        speedToolbar.add(limit10Button);

        // End of limit: value -1
        JButton endOfLimitButton = new JButton("End of limit");
        allComponents.add(endOfLimitButton);
        endOfLimitButton.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            world.setSpeedLimitValue(-1);
        });
        speedToolbar.add(endOfLimitButton);

        // No limit information in this cell: value Integer.MAX_VALUE
        JButton noLimitButton = new JButton("No limit");
        allComponents.add(noLimitButton);
        noLimitButton.addActionListener((e) -> {
            panel.setTool(GuiTool.SPEED_INDICATOR);
            world.setSpeedLimitValue(Integer.MAX_VALUE);
        });
        speedToolbar.add(noLimitButton);

        speedIndicatorTextField = new JTextField();
        allComponents.add(speedIndicatorTextField);
        speedIndicatorTextField.setPreferredSize(new Dimension(80, 27));
        speedIndicatorTextField.setText("30.0");
        speedIndicatorTextField.addActionListener((e) -> {
            readSpeedLimit();
            panel.setTool(GuiTool.SPEED_INDICATOR);
        });
        speedToolbar.add(speedIndicatorTextField);

        JButton speedCellButton = new JButton("Custom speed limit");
        allComponents.add(speedCellButton);
        speedCellButton.addActionListener((e) -> {
            readSpeedLimit();
            panel.setTool(GuiTool.SPEED_INDICATOR);
        });
        speedToolbar.add(speedCellButton);

        stopTimerTextField = new JTextField();
        allComponents.add(stopTimerTextField);
        stopTimerTextField.setPreferredSize(new Dimension(80, 27));
        stopTimerTextField.setText("" + defaultStopTimerDuration);
        stopTimerTextField.addActionListener((e) -> {
            readStopTimer();
        });
        speedToolbar.add(stopTimerTextField);

        JButton stopTimerButton = new JButton("Stop Timer");
        allComponents.add(stopTimerButton);
        stopTimerButton.addActionListener((e) -> {
            readStopTimer();
            panel.setTool(GuiTool.STOP_TIMER);
        });
        speedToolbar.add(stopTimerButton);

        mainToolbar.add(speedToolbar);

        for (Component comp : allComponents) {
            comp.setFocusable(true);
            comp.addKeyListener(keyListener);
        }

        world.addPropertyChangeListener("currentStep", panel);
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
        System.out.println("GUI Saving to file.");

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

                try {
                    FileWriter writer = new FileWriter(file);
                    panel.save(writer);
                    world.save(writer);
                    writer.close();
                } catch (IOException e) {
                    System.out.println("Error writing to file.");
                }
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
                System.out.println("GUI Loading from file " + file.getAbsolutePath());

                scanner = new Scanner(file);
                panel.load(scanner);
                world.load(scanner);
                repaint();
            }
        } catch (FileNotFoundException ex) {
            // No config file, maybe create it here.
            System.out.println("Cannot load, no config file.");
        }
        worldMapLabel.setText(World.map.toFormattedString());
        System.out.println("GUI end load.");
    }

    protected void setControlState(boolean b) {
        panel.setControlState(b);
    }
}
