package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.ui.range.slider.RangeSlider;

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
    protected IntactNetworkView currentIView;
    protected Map<CyNetwork, Map<String, Map<String, Double>>> filters;

    public AbstractIntactPanel(final IntactManager manager) {
        this.manager = manager;
        this.openBrowser = manager.getService(OpenBrowser.class);
        this.currentNetwork = manager.getCurrentNetwork();
        currentIView = manager.getCurrentIntactNetworkView();
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
        JPanel panel = new JPanel(new GridBagLayout());
        EasyGBC c = new EasyGBC();
        if (labels) {
            JLabel label = new JLabel(text);
            label.setFont(labelFont);
            label.setPreferredSize(new Dimension(80, 20));
            label.setMaximumSize(new Dimension(80, 20));
//            panel.add(Box.createRigidArea(new Dimension(10, 0)));
            panel.add(label, c.anchor("west").noExpand());
//            panel.add(Box.createHorizontalGlue());
        }

        RangeSlider slider = new RangeSlider(0, 100);
        slider.setValue(0);
        slider.setUpperValue(100);
        slider.setOpaque(true);

        panel.add(slider, c.right().expandHoriz().anchor("west"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
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

    public void networkViewChanged(CyNetworkView view) {
        currentIView = manager.getIntactNetworkView(view);
    }

    protected void addFilter(String type, String label, double value) {
        Map<String, Double> filter = filters.get(currentNetwork).get(type);
        filter.put(label, value);

        if (value == 0)
            filter.remove(label);
    }
}
