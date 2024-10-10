package minimetro;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This tool represents a button with a text field.
 *
 * @author arthu
 */
public class ViewpointPanel extends JPanel {

    protected JButton b;
    protected JTextField t;
    protected ViewPoint viewPoint;
    protected JButton overwriteButton;
    protected JButton deleteButton;
    protected WorldPanel worldPanel;

    private PropertyChangeSupport support;

    private int id;
    private static int NB_TEXT_BUTTONS_CREATED = 0;

    public ViewpointPanel(String text1, WorldPanel newWorldPanel, String newView) {
        super();
        id = NB_TEXT_BUTTONS_CREATED;
        NB_TEXT_BUTTONS_CREATED++;
        b = new JButton(text1 + id);
        b.addActionListener((e) -> {
            worldPanel.setView(viewPoint);
            worldPanel.repaint();
        });
        this.add(b);

        t = new JTextField("view");
        t.setInputVerifier(new CustomInputVerifier());
        this.add(t);

        overwriteButton = new JButton("update");
        overwriteButton.addActionListener((e) -> {
            viewPoint = new ViewPoint(worldPanel.getView());
        });
        this.add(overwriteButton);
        worldPanel = newWorldPanel;
        if (newView.equals("")) {
            viewPoint = new ViewPoint(worldPanel.getView());
        } else {
            viewPoint = new ViewPoint(newView);
        }

        support = new PropertyChangeSupport(this);

        deleteButton = new JButton("x");
        deleteButton.addActionListener((e) -> {
            support.firePropertyChange("deleteViewpointPanel", -1, id);
        });
        this.add(deleteButton);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    public String getText() {
        return b.getText();
    }

    public void save(FileWriter writer) {

        String text = "";
        try {
            text += viewPoint.getX() + " " + viewPoint.getY() + " " + viewPoint.getZoom();
            text += "\n";
            writer.write(text);
        } catch (IOException ex) {
            System.out.println("Error in saving viewpointPanel");
        }
    }

    private static class CustomInputVerifier extends InputVerifier {

        public CustomInputVerifier() {
        }

        @Override
        public boolean verify(JComponent input) {
            return true;
        }
    }

    public int getId() {
        return id;
    }
}
