package uk.ac.ebi.intact.intactApp.internal.ui.panels.east;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.RangeSlider;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays information about a protein taken from STRING
 *
 * @author Scooter Morris
 */
public abstract class AbstractDetailPanel extends JPanel {
    public final static Color backgroundColor = new Color(251, 251, 251);
    public final static Font labelFont = new Font("SansSerif", Font.BOLD, 12);
    public final static Font textFont = new Font("SansSerif", Font.PLAIN, 12);

    protected final IntactManager manager;
    protected final OpenBrowser openBrowser;
    protected final Font iconFont;
    protected IntactNetwork currentINetwork;
    protected IntactNetworkView currentIView;
    protected Map<CyNetwork, Map<String, Map<String, Double>>> filters = new HashMap<>();
    protected final LimitExceededPanel limitExceededPanel;


    public AbstractDetailPanel(final IntactManager manager, int selectionLimit, String limitOfWhat) {
        this.manager = manager;
        this.openBrowser = manager.getService(OpenBrowser.class);
        this.currentINetwork = manager.getCurrentIntactNetwork();
        this.currentIView = manager.getCurrentIntactNetworkView();
        setBackground(backgroundColor);
        IconManager iconManager = manager.getService(IconManager.class);
        iconFont = iconManager.getIconFont(17.0f);
        limitExceededPanel = new LimitExceededPanel(limitOfWhat, selectionLimit);
        //        filters.put(currentINetwork.getNetwork(), new HashMap<>());
    }

    protected abstract void doFilter(String type);

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

    protected void addFilter(String type, String label, double value) {
        Map<String, Double> filter = filters.get(currentINetwork.getNetwork()).get(type);
        filter.put(label, value);

        if (value == 0)
            filter.remove(label);
    }

    protected static class LimitExceededPanel extends JPanel {
        public LimitExceededPanel(String limitOfWhat, int limit) {
            setLayout(new GridLayout(2, 1));
            Font font = textFont.deriveFont(15f);
            JLabel label = new JLabel(String.format("More than %d %s selected", limit, limitOfWhat));
            label.setForeground(Color.WHITE);
            label.setFont(font);
            label.setHorizontalAlignment(JLabel.CENTER);
            add(label);
            JLabel label1 = new JLabel(String.format("For details on other %s, please select less %s", limitOfWhat, limitOfWhat));
            label1.setFont(font);
            label1.setForeground(Color.WHITE);
            label1.setHorizontalAlignment(JLabel.CENTER);
            add(label1);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int height = getHeight();
            g2.setPaint(new LinearGradientPaint(0, 0, 0, height,
                    new float[]{0.0f, 1f},
                    new Color[]{new Color(66, 42, 146), new Color(134, 56, 148),}));
            g2.fillRoundRect(0, 0, getWidth(), height, 10, 10);
        }
    }
}
