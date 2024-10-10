package minimetro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.BoxLayout;
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
public class GUI extends JFrame implements PropertyChangeListener {

    WorldPanel panel;
    JPanel topToolbar;
    JPanel locoToolbar;
    JPanel speedToolbar;
    JPanel viewsPanel;
    JPanel subVPPanel;
    JPanel mainToolbar;
    JScrollPane mainToolbarScrollpane;
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

        mainToolbarScrollpane = new JScrollPane(mainToolbar,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        allComponents.add(mainToolbarScrollpane);
        mainToolbar.setLayout(new BoxLayout(mainToolbar, BoxLayout.Y_AXIS));

        topToolbar = new JPanel();
        allComponents.add(topToolbar);
        topToolbar.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;

        JButton playPauseButton = new JButton("PlayPause");
        allComponents.add(playPauseButton);
        playPauseButton.addActionListener((e) -> {
            world.togglePlayPause();
        });
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        topToolbar.add(playPauseButton, c);

        JButton stepButton = new JButton("Step");
        allComponents.add(stepButton);
        stepButton.addActionListener((e) -> {
            world.step();
            repaint();
        });
        c.gridx = 1;
        c.gridy = 0;
        topToolbar.add(stepButton, c);

        JButton trackButton = new JButton("Track");
        allComponents.add(trackButton);
        trackButton.addActionListener((e) -> {
            panel.setTool(GuiTool.TRACK);
        });
        c.gridx = 0;
        c.gridy = 1;
        topToolbar.add(trackButton, c);

        JButton longDistanceTracksButton = new JButton("Long distance tracks");
        allComponents.add(longDistanceTracksButton);
        longDistanceTracksButton.addActionListener((e) -> {
            panel.setTool(GuiTool.LONG_DISTANCE_TRACKS);
        });
        c.gridx = 1;
        c.gridy = 1;
        topToolbar.add(longDistanceTracksButton, c);

        JButton switchButton = new JButton("Switch");
        allComponents.add(switchButton);
        switchButton.addActionListener((e) -> {
            panel.setTool(GuiTool.SWITCH);
        });
        c.gridx = 0;
        c.gridy = 2;
        topToolbar.add(switchButton, c);

        JButton removeTrackButton = new JButton("Remove Track");
        allComponents.add(removeTrackButton);
        removeTrackButton.addActionListener((e) -> {
            panel.setTool(GuiTool.TRACK_REMOVAL);
        });
        c.gridx = 1;
        c.gridy = 2;
        topToolbar.add(removeTrackButton, c);

        JButton stationButton = new JButton("Station");
        allComponents.add(stationButton);
        stationButton.addActionListener((e) -> {
            panel.setTool(GuiTool.STATION);
        });
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        topToolbar.add(stationButton, c);

        JButton locoButton = new JButton("Loco");
        allComponents.add(locoButton);
        locoButton.addActionListener((e) -> {
            panel.setTool(GuiTool.LOCO);
        });
        c.gridx = 1;
        c.gridy = 3;
        topToolbar.add(locoButton, c);

        JButton wagonButton = new JButton("Wagon");
        allComponents.add(wagonButton);
        wagonButton.addActionListener((e) -> {
            panel.setTool(GuiTool.WAGON);
        });
        c.gridx = 0;
        c.gridy = 4;
        topToolbar.add(wagonButton, c);

        JButton removeTrainsButton = new JButton("Remove trains");
        allComponents.add(removeTrainsButton);
        removeTrainsButton.addActionListener((e) -> {
            panel.setTool(GuiTool.TRAIN_REMOVAL);
        });
        c.gridx = 1;
        c.gridy = 4;
        topToolbar.add(removeTrainsButton, c);

        JButton zoomInButton = new JButton("Zoom in");
        allComponents.add(zoomInButton);
        zoomInButton.addActionListener((e) -> {
            panel.zoomIn();
            panel.repaint();
        });
        c.gridx = 0;
        c.gridy = 5;
        topToolbar.add(zoomInButton, c);

        JButton zoomOutButton = new JButton("Zoom out");
        allComponents.add(zoomOutButton);
        zoomOutButton.addActionListener((e) -> {
            panel.zoomOut();
            panel.repaint();
        });
        c.gridx = 1;
        c.gridy = 5;
        topToolbar.add(zoomOutButton, c);

        JButton boardButton = new JButton("Board");
        allComponents.add(boardButton);
        boardButton.addActionListener((e) -> {
            world.boardPassengers();
        });
        c.gridx = 0;
        c.gridy = 6;
        topToolbar.add(boardButton, c);

        JButton getOffButton = new JButton("Get off");
        allComponents.add(getOffButton);
        getOffButton.addActionListener((e) -> {
            world.getPassengersOff();
        });
        c.gridx = 1;
        c.gridy = 6;
        topToolbar.add(getOffButton, c);

        JButton generatePassengersButton = new JButton("Generate Passengers");
        allComponents.add(generatePassengersButton);
        generatePassengersButton.addActionListener((e) -> {
            world.generatePassengers();
            panel.repaint();
        });
        c.gridx = 0;
        c.gridy = 7;
        topToolbar.add(generatePassengersButton, c);

        JButton removePassengersButton = new JButton("Remove Passengers");
        allComponents.add(removePassengersButton);
        removePassengersButton.addActionListener((e) -> {
            world.removePassengers();
            panel.repaint();
        });
        c.gridx = 1;
        c.gridy = 7;
        topToolbar.add(removePassengersButton, c);

        JButton saveButton = new JButton("Save");
        allComponents.add(saveButton);
        saveButton.addActionListener((e) -> {
            save();
        });
        c.gridx = 0;
        c.gridy = 8;
        topToolbar.add(saveButton, c);

        JButton loadButton = new JButton("Load");
        allComponents.add(loadButton);
        loadButton.addActionListener((e) -> {
            load();
        });
        c.gridx = 1;
        c.gridy = 8;
        topToolbar.add(loadButton, c);

        mainToolbar.add(topToolbar);

        locoToolbar = new JPanel();
        allComponents.add(locoToolbar);
        locoToolbar.setLayout(new GridBagLayout());

        JPanel locoSubMenu = new JPanel();
        locoSubMenu.setLayout(new GridLayout(2, 2));
        JButton startLocoButton = new JButton("Start Locos");
        allComponents.add(startLocoButton);
        startLocoButton.addActionListener((e) -> {
            world.startLocos();
        });
        locoSubMenu.add(startLocoButton, c);

        JButton stopLocoButton = new JButton("Stop Locos");
        allComponents.add(stopLocoButton);
        stopLocoButton.addActionListener((e) -> {
            world.stopLocos();
        });
        locoSubMenu.add(stopLocoButton, c);

        JButton worldMapButton = new JButton("Display World Map");
        allComponents.add(worldMapButton);
        locoSubMenu.add(worldMapButton, c);

        JButton clearMapButton = new JButton("Clear Map");
        allComponents.add(clearMapButton);
        clearMapButton.addActionListener((e) -> {
            world.clearMap();
            worldMapLabel.setText(World.map.toFormattedString());
        });
        locoSubMenu.add(clearMapButton, c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        locoToolbar.add(locoSubMenu, c);

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
        mapScrollPane.setMinimumSize(new Dimension(200, 150));
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        locoToolbar.add(mapScrollPane, c);

        JLabel startStationIdLabel = new JLabel("Start station");
        allComponents.add(startStationIdLabel);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        locoToolbar.add(startStationIdLabel, c);

        JTextField startStationIdTextField = new JTextField("0");
        allComponents.add(startStationIdTextField);
        c.gridx = 0;
        c.gridy = 4;
        locoToolbar.add(startStationIdTextField, c);

        JLabel endStationIdLabel = new JLabel("End station");
        allComponents.add(endStationIdLabel);
        c.gridx = 1;
        c.gridy = 3;
        locoToolbar.add(endStationIdLabel, c);

        JTextField endStationIdTextField = new JTextField("0");
        allComponents.add(endStationIdTextField);
        c.gridx = 1;
        c.gridy = 4;
        locoToolbar.add(endStationIdTextField, c);

        JLabel nbPassengersLabel = new JLabel("Nb passengers");
        allComponents.add(nbPassengersLabel);
        c.gridx = 2;
        c.gridy = 3;
        locoToolbar.add(nbPassengersLabel, c);

        JTextField nbPassengersTextField = new JTextField("1");
        allComponents.add(nbPassengersTextField);
        c.gridy = 4;
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

        viewsPanel = new JPanel();
        viewsPanel.setLayout(new BoxLayout(viewsPanel, BoxLayout.Y_AXIS));
        viewsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewsPanel.add(new JLabel("Viewpoints"));
        subVPPanel = new JPanel();
        subVPPanel.setLayout(new BoxLayout(subVPPanel, BoxLayout.Y_AXIS));
        subVPPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton newViewPointButton = new JButton("Add viewpoint");
        newViewPointButton.addActionListener((e) -> {
            createViewPoint();
        });
        viewsPanel.add(subVPPanel);
        viewsPanel.add(newViewPointButton);

        mainToolbar.add(viewsPanel);

        mainToolbarScrollpane.setForeground(Color.red);
        mainToolbarScrollpane.setFocusable(true);

        this.add(mainToolbarScrollpane, BorderLayout.WEST);

        for (Component comp : allComponents) {
            comp.setFocusable(true);
            comp.addKeyListener(keyListener);
        }

        world.addPropertyChangeListener("currentStep", panel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(windowWidth, windowHeight));
        this.setVisible(true);

    }

    private void createViewPoint() {
        createViewPoint("");
    }

    private void createViewPoint(String specifiedView) {
        ViewpointPanel newViewPanel = new ViewpointPanel("Go", panel, specifiedView);
        newViewPanel.addPropertyChangeListener("deleteViewpointPanel", this);
        subVPPanel.add(newViewPanel);

        mainToolbar.revalidate();
        Rectangle bounds = viewsPanel.getBounds();
        viewsPanel.scrollRectToVisible(bounds);
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
                    saveViewpoints(writer);
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

                subVPPanel.removeAll();

                scanner = new Scanner(file);
                panel.load(scanner);
                world.load(scanner);
                loadViewpoints(scanner);
                revalidate();
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

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        int viewpointPanelId = (int) e.getNewValue();
        deleteViewpoint(viewpointPanelId);
    }

    private void deleteViewpoint(int idToDelete) {
        Component[] allViewpoints = subVPPanel.getComponents();
        for (int rank = 0; rank < allViewpoints.length; rank++) {
            Component yolo = allViewpoints[rank];
            ViewpointPanel viewpointPanel = (ViewpointPanel) yolo;
            if (viewpointPanel.getId() == idToDelete) {
                subVPPanel.remove(yolo);
            }
        }
        revalidate();
    }

    private void saveViewpoints(FileWriter writer) {

        try {
            writer.write("viewpoints\n");

            Component[] allViewpoints = subVPPanel.getComponents();
            for (int rank = 0; rank < allViewpoints.length; rank++) {
                Component yolo = allViewpoints[rank];
                ViewpointPanel viewpointPanel = (ViewpointPanel) yolo;
                viewpointPanel.save(writer);
            }
            writer.write("endviewpoints\n");
        } catch (IOException ex) {
            System.out.println("GUI error while writing viewpoints");
        }
    }

    private void loadViewpoints(Scanner scanner) {
        String text = "";
        boolean loop = true;
        while (scanner.hasNextLine() && loop) {
            text = scanner.nextLine();
            if (text.equals("endviewpoints")) {
                loop = false;
            } else if (!text.equals("viewpoints")) {
                String[] split = text.split(" ");
                createViewPoint(text);
            }
        }
    }
}
