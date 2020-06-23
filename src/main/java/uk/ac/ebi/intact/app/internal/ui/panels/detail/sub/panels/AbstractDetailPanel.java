package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LimitExceededPanel;

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

    protected final Manager manager;
    protected final OpenBrowser openBrowser;
    protected final Font iconFont;
    protected Network currentINetwork;
    protected NetworkView currentIView;
    protected Map<CyNetwork, Map<String, Map<String, Double>>> filters = new HashMap<>();
    protected final LimitExceededPanel limitExceededPanel;


    public AbstractDetailPanel(final Manager manager, int selectionLimit, String limitOfWhat) {
        this.manager = manager;
        this.openBrowser = manager.utils.getService(OpenBrowser.class);
        this.currentINetwork = manager.data.getCurrentNetwork();
        this.currentIView = manager.data.getCurrentIntactNetworkView();
        setBackground(backgroundColor);
        IconManager iconManager = manager.utils.getService(IconManager.class);
        iconFont = iconManager.getIconFont(17.0f);
        limitExceededPanel = new LimitExceededPanel(limitOfWhat, "selected", selectionLimit, "select less " + limitOfWhat);
    }

    protected boolean checkCurrentNetwork() {
        if (currentINetwork == null) {
            currentINetwork = manager.data.getCurrentNetwork();
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

}
