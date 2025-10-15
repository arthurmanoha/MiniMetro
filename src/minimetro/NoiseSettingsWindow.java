package minimetro;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author arthu
 */
public class NoiseSettingsWindow extends JFrame implements ChangeListener {

    private World w;
    private ArrayList<JSlider> sliders;
    private ArrayList<JLabel> labels;

    private int nbLevels = 7;

    private int min = 0;
    private int max = 100;

    public NoiseSettingsWindow(World newWorld) {
        super();

        w = newWorld;
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(10, 10, 10, 10);

        int startValue = 100;
        sliders = new ArrayList<>();

        for (int level = 0; level < nbLevels; level++) {
            JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, startValue);
            slider.addChangeListener(this);
            c.gridx = 0;
            c.gridy = level;
            JLabel label = new JLabel("" + level);
            this.add(label, c);
            c.gridx = 1;
            this.add(slider, c);
            sliders.add(slider);
        }

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(false);
        this.pack();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            for (int level = 0; level < nbLevels; level++) {
                int newValue = sliders.get(level).getValue();
                w.setAmplitudes(newValue, level);
            }
        }
        if (!source.getValueIsAdjusting()) {
            w.computeAltitudes();
        }
    }
}
