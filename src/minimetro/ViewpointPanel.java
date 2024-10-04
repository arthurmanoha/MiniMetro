package minimetro;

import java.awt.event.ActionListener;
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
    protected WorldPanel worldPanel;

    private int id;
    private static int NB_TEXT_BUTTONS_CREATED = 0;

    public ViewpointPanel(String text1, WorldPanel newWorldPanel) {
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
        viewPoint = new ViewPoint(worldPanel.getView());
    }

    public String getText() {
        return b.getText();
    }

    private static class CustomInputVerifier extends InputVerifier {

        public CustomInputVerifier() {
        }

        @Override
        public boolean verify(JComponent input) {
            return true;
        }
    }
}
