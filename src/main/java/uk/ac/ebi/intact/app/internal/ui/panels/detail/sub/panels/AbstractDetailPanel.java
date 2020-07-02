package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
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
    public final static Font labelFont = new Font("SansSerif", Font.BOLD, 12);
    public final static Font textFont = new Font("SansSerif", Font.PLAIN, 12);

    protected final Manager manager;
    protected final OpenBrowser openBrowser;
    protected final Font iconFont;
    protected Network currentNetwork;
    protected NetworkView currentView;
    protected Map<CyNetwork, Map<String, Map<String, Double>>> filters = new HashMap<>();
    protected final LimitExceededPanel limitExceededPanel;


    public AbstractDetailPanel(final Manager manager, int selectionLimit, String limitOfWhat) {
        this.manager = manager;
        this.openBrowser = manager.utils.getService(OpenBrowser.class);
        this.currentNetwork = manager.data.getCurrentNetwork();
        this.currentView = manager.data.getCurrentIntactNetworkView();
        setBackground(UIColors.lightBackground);
        IconManager iconManager = manager.utils.getService(IconManager.class);
        iconFont = iconManager.getIconFont(17.0f);
        limitExceededPanel = new LimitExceededPanel(limitOfWhat, "selected", selectionLimit, "select less " + limitOfWhat);
    }

    protected boolean checkCurrentNetwork() {
        if (currentNetwork == null) {
            currentNetwork = manager.data.getCurrentNetwork();
            return currentNetwork != null;
        }
        return true;
    }

    protected boolean checkCurrentView() {
        if (currentView == null) {
            currentView = manager.data.getCurrentIntactNetworkView();
            return currentView != null;
        }
        return true;
    }

}
