package minimetro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author arthu
 */
public class GUI extends JFrame implements PropertyChangeListener {

    private String configFileName = "config.txt";
    private File configFile;

    WorldPanel panel;
    JPanel buttonPanel;
    JPanel topToolbar;
    JPanel locoToolbar;
    JPanel speedToolbar;
    JPanel viewsPanel;
    JPanel subVPPanel;
    JPanel mainToolbar;
    JScrollPane mainToolbarScrollpane;
    JLabel worldMapLabel;
    JTextField itineraryTextField;

    int windowWidth = 1100;
    int windowHeight = 1035;

    private World world;
    private JTextField speedIndicatorTextField;
    private JTextField stopTimerTextField;
    double defaultStopTimerDuration = 5;

    private CustomKeyListener keyListener;
    private ArrayList<Component> allComponents;

    public GUI(World newWorld) {
        super();

        world = newWorld;
        panel = new WorldPanel(world);

        this.add(panel, BorderLayout.CENTER);

        createMenu();

        world.addPropertyChangeListener("currentStep", panel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(windowWidth, windowHeight));
        this.setVisible(true);
        configFile = new File(configFileName);
        autoLoad();
    }

    /**
     * Automatically load a saved file if the configuration file exists and
     * specifies it.
     *
     */
    private void autoLoad() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Step one: read the config file.
                String savedFilesFolder;
                String autoLoadedFile = "";

                try {
                    Scanner scanner = new Scanner(configFile);

                    savedFilesFolder = scanner.nextLine();
                    autoLoadedFile = scanner.nextLine();

                    File savedWorld = new File(savedFilesFolder + autoLoadedFile);
                    // Step two: read the world file.
                    scanner = new Scanner(savedWorld);

                    panel.load(scanner);
                    world.load(scanner);
                    repaint();
                } catch (FileNotFoundException ex) {
                    System.out.println("No file named <" + autoLoadedFile + ">");
                } catch (NoSuchElementException ex) {
                    System.out.println("No correct autofile was specified.");
                }
            }
        });
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

    public void save() {

        // Read the config file to know where to look
        try {
            Scanner scanner = new Scanner(configFile);
            String text = "";
            String savedFilesFolder = "";
            if (scanner.hasNextLine()) {
                savedFilesFolder = scanner.nextLine();
            }
            JFileChooser fileChooser = new JFileChooser(savedFilesFolder);
            fileChooser.setDialogTitle("Save...");
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
            System.out.println("File <" + configFileName + "> not found.");
        }
    }

    public void load() {
        System.out.println("GUI.load()");
        // Read the config file to know where to look
        try {
            Scanner scanner = new Scanner(configFile);
            String text = "";
            if (scanner.hasNextLine()) {
                text = scanner.nextLine();
            }
            JFileChooser fileChooser = new JFileChooser(text);
            fileChooser.setDialogTitle("Load...");
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                scanner = new Scanner(file);
                panel.load(scanner);
                world.load(scanner);
                revalidate();
            }
        } catch (FileNotFoundException ex) {
            // No config file, maybe create it here.
            System.out.println("Cannot load, no config file.");
        }
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

    private void createMenu() {

        buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(0, 0, 0, 0));
        buttonPanel.setLayout(new GridBagLayout());

        createButton("menu_play_pause.png", "togglePlayPause", 0, 0);
        createButton("menu_step.png", "step", 0, 1);
        createButton("rails.png", GuiTool.TRACK.toString(), 1, 0);
        createButton("rails_delete.png", GuiTool.TRACK_REMOVAL.toString(), 2, 0);
        createButton("long_distance_rails.png", GuiTool.LONG_DISTANCE_TRACKS.toString(), 1, 1, 2, "");
        createButton("switch.png", GuiTool.SWITCH.toString(), 3, 0);
        createButton("station.png", GuiTool.STATION.toString(), 4, 0);
        createButton("loco.png", GuiTool.LOCO.toString(), 3, 1);
        createButton("wagon.png", GuiTool.WAGON.toString(), 4, 1);
        createButton("remove_train.png", "removeTrain", 5, 1);
        createButton("generate_passengers.png", "generatePassengers", 6, 1);
        createButton("remove_passengers.png", "removePassengers", 7, 1);
        // Speed limits: Integer.MAX_VALUE if not set, -1 for end of limit, >0 for actual limit.
        createButton("limit_10.png", GuiTool.SPEED_INDICATOR.toString(), 5, 0, 1, "2");
        createButton("limit_50.png", GuiTool.SPEED_INDICATOR.toString(), 6, 0, 1, "10");
        createButton("limit_100.png", GuiTool.SPEED_INDICATOR.toString(), 7, 0, 1, "50");
        createButton("end_of_limit.png", GuiTool.SPEED_INDICATOR.toString(), 8, 0, 1, "-1");
        createButton("no_limit.png", GuiTool.SPEED_INDICATOR.toString(), 9, 0, 1, "Integer.MAX_VALUE");
        createButton("menu_save.png", "save", 8, 1);
        createButton("menu_load.png", "load", 9, 1);
        createButton("stop_10.png", GuiTool.STOP_TIMER.toString(), 10, 0, 1, "2");
        createButton("stop_30.png", GuiTool.STOP_TIMER.toString(), 10, 1, 1, "10");
        createButton("generate_terrain.png", "displayTerrain", 11, 0);

        System.out.println("Creating itineraryTextField");
        itineraryTextField = new JTextField("", 3);
        itineraryTextField.setSize(10, 10);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 11;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        buttonPanel.add(itineraryTextField, constraints);

        panel.setLayout(new BorderLayout());
        panel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Create a button with the appropriate icon and link it with the specified
     * method.
     *
     * @param iconName name of the file
     * @param toolOrFunction name of the function that will be called on click
     * @param gridx column in the gridBagLayout
     * @param gridy row in the gridBagLayout
     */
    private void createButton(String iconName, String toolOrFunction, int gridx, int gridy) {
        createButton(iconName, toolOrFunction, gridx, gridy, 1, "");
    }

    private void createButton(String iconName, String toolOrFunction, int gridx, int gridy, int gridWidth, String parameter) {
        String iconsPath = "src\\menuIcons\\";

        JButton newButton;
        String currentIconPath = iconsPath + iconName;
        Path path = Paths.get(currentIconPath);
        ImageIcon icon;

        if (Files.exists(path)) {
            icon = new ImageIcon(currentIconPath);
            newButton = new JButton(icon);
            newButton.setSize(icon.getIconWidth(), icon.getIconHeight());
            newButton.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            newButton.setBackground(new Color(0, 0, 0, 0));
        } else {
            newButton = new JButton(iconName);
            newButton.setBackground(new Color(255, 255, 255));
        }

        try {
            GuiTool newTool = GuiTool.valueOf(toolOrFunction);
            newButton.addActionListener((e) -> {
                panel.setTool(newTool);
                if (iconName.contains("limit") && !parameter.isEmpty()) {
                    double newSpeedLimit;
                    if (parameter.equals("Integer.MAX_VALUE")) {
                        newSpeedLimit = Integer.MAX_VALUE;
                    } else {
                        newSpeedLimit = Double.parseDouble(parameter);
                    }
                    world.setSpeedLimitValue(newSpeedLimit);
                } else if (iconName.contains("stop") && !parameter.isEmpty()) {
                    int stopDuration = Integer.parseInt(parameter);
                    world.setStopTimerDuration(stopDuration);
                }
            });
        } catch (IllegalArgumentException exc) {
            // Not a GuiTool, so it's a function
            Class currentClass = this.getClass();
            try {
                Method method = currentClass.getMethod(toolOrFunction);
                if (method.getParameterCount() == 0) {
                    newButton.addActionListener((e) -> {
                        try {
                            method.invoke(this);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            System.out.println("Error: method " + method.getName() + " from class " + method.getDeclaringClass());
                            Logger.getLogger(WorldPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                } else {
                    newButton.addActionListener((e) -> {
                        try {
                            method.invoke(this, parameter);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(WorldPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                }
            } catch (NoSuchMethodException | SecurityException ex) {
                Logger.getLogger(WorldPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridWidth;
        buttonPanel.add(newButton, constraints);
    }

    public void togglePlayPause() {
        world.togglePlayPause();
    }

    public void step() {
        world.step();
    }

    public void removeTrain() {
        panel.setTool(GuiTool.TRAIN_REMOVAL);
    }

    public void generatePassengers() {
        if (!itineraryTextField.getText().isBlank() && !itineraryTextField.getText().isEmpty()) {
            String textTab[] = itineraryTextField.getText().split(" ");
            System.out.println("Reading tab");
            int startStation = -1;
            if (textTab.length > 0) {
                startStation = Integer.valueOf(textTab[0]);
            }
            System.out.println("start station: " + startStation);
            int targetStation = -1;
            if (textTab.length > 1) {
                targetStation = Integer.valueOf(textTab[1]);
            }
            System.out.println("target station: " + targetStation);
            int nbPassengers = -1;
            if (textTab.length > 2) {
                nbPassengers = Integer.valueOf(textTab[2]);
            }
            System.out.println("nb passengers: " + nbPassengers);

            System.out.println("Creating " + nbPassengers + " passenger"
                    //                    + (nbPassengers >= 2 ? "s" : "") // Plural please
                    + " at " + startStation + " going to " + targetStation);
            System.out.println("yolo");

            System.out.println("calling world.generatePassengers("
                    + startStation + ", " + targetStation + ", " + nbPassengers + ");");
            world.generatePassengers(startStation, targetStation, nbPassengers);
            System.out.println("end world.generatePassengers");

        } else {
            System.out.println("Generating random passengers");
            world.generatePassengers();
        }
        panel.repaint();
        System.out.println("End generatePssengers");
    }

    public void removePassengers() {
        world.removePassengers();
    }

    public void displayTerrain() {
        panel.toggleDisplayTerrain();
    }

}
