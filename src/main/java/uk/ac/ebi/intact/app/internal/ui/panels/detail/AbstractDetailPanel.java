package uk.ac.ebi.intact.app.internal.ui.panels.detail;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.IntactNetwork;
import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;

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
        this.openBrowser = manager.utils.getService(OpenBrowser.class);
        this.currentINetwork = manager.data.getCurrentIntactNetwork();
        this.currentIView = manager.data.getCurrentIntactNetworkView();
        setBackground(backgroundColor);
        IconManager iconManager = manager.utils.getService(IconManager.class);
        iconFont = iconManager.getIconFont(17.0f);
        limitExceededPanel = new LimitExceededPanel(limitOfWhat, selectionLimit);
        //        filters.put(currentINetwork.getNetwork(), new HashMap<>());
    }

    protected boolean checkCurrentNetwork() {
        if (currentINetwork == null) {
            currentINetwork = manager.data.getCurrentIntactNetwork();
            return currentINetwork != null;
        }
        return true;
    }

    protected boolean checkCurrentView() {
        if (currentIView == null) {
            currentIView = manager.data.getCurrentIntactNetworkView();
            return currentIView != null;
        }
        return true;
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
