package uk.ac.ebi.intact.app.internal.utils;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public class ViewUtils {
    public static CyNetworkView registerView(Manager manager, CyNetworkView netView) {
        if (netView != null) {
            manager.utils.getService(CyNetworkViewManager.class).addNetworkView(netView);
            manager.utils.getService(CyApplicationManager.class).setCurrentNetworkView(netView);
        }
        return netView;
    }
}
