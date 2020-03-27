package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays information about a protein taken from STRING
 *
 * @author Scooter Morris
 */
public abstract class AbstractIntactPanel extends JPanel {

    protected final IntactManager manager;
    protected final OpenBrowser openBrowser;
    protected final Font iconFont;
    protected final Font labelFont;
    protected final Font textFont;
    protected CyNetwork currentNetwork;
    protected Map<CyNetwork, Map<String, Map<String, Double>>> filters;

    public AbstractIntactPanel(final IntactManager manager) {
        this.manager = manager;
        this.openBrowser = manager.getService(OpenBrowser.class);
        this.currentNetwork = manager.getCurrentNetwork();
        IconManager iconManager = manager.getService(IconManager.class);
        iconFont = iconManager.getIconFont(17.0f);
        labelFont = new Font("SansSerif", Font.BOLD, 10);
        textFont = new Font("SansSerif", Font.PLAIN, 10);
        filters = new HashMap<>();
        filters.put(currentNetwork, new HashMap<>());
    }

    abstract void doFilter(String type);

    protected JComponent createFilterSlider(String type, String text, CyNetwork network, boolean labels, double max) {
        double value = 0.0;
        if (filters.containsKey(network) &&
                filters.get(network).containsKey(type) &&
                filters.get(network).get(type).containsKey(text)) {
            value = filters.get(network).get(type).get(text);
            // System.out.println("value = "+value);
        }
        Box box = Box.createHorizontalBox();
        if (labels) {
            JLabel label = new JLabel(text);
            label.setFont(labelFont);
            label.setPreferredSize(new Dimension(100, 20));
//            box.add(Box.createRigidArea(new Dimension(10, 0)));
            box.add(label);
            box.add(Box.createHorizontalGlue());
        }
        JSlider slider;
        slider = new JSlider(0, (int) max, (int) (value * 100));

//        slider.setPreferredSize(new Dimension(100, 20));
        slider.setSize(new Dimension(80, 20));
        box.add(slider);
        // box.add(Box.createHorizontalGlue());
        JTextField textField;
        textField = new JTextField(String.format("%.2f", value), 4);
//        textField.setPreferredSize(new Dimension(30, 20));
        textField.setSize(new Dimension(15, 20));
        textField.setMaximumSize(new Dimension(15, 20));
        textField.setFont(textFont);
        box.add(textField);
        // Hook it up
        addChangeListeners(type, text, slider, textField, max);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        return box;
    }

    protected void addChangeListeners(String type, String label, JSlider slider,
                                      JTextField textField, double max) {
        slider.addChangeListener(e -> {
            JSlider sl = (JSlider) e.getSource();
            int value = sl.getValue();
            double v = ((double) value) / 100.0;
            textField.setText(String.format("%.2f", v));
            addFilter(type, label, v);
            doFilter(type);
        });

        textField.addActionListener(e -> {
            JTextField field = (JTextField) e.getSource();
            String text = field.getText();
            slider.setValue((int) (Double.parseDouble(text) * 100.0));
        });
    }

    protected void addFilter(String type, String label, double value) {
        Map<String, Double> filter = filters.get(currentNetwork).get(type);
        filter.put(label, value);

        if (value == 0)
            filter.remove(label);
    }
}