package uk.ac.ebi.intact.app.internal.utils;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ViewUtils {
    public static CyNetworkView registerView(Manager manager, CyNetworkView netView) {
        if (netView != null) {
            manager.utils.getService(CyNetworkViewManager.class).addNetworkView(netView);
            manager.utils.getService(CyApplicationManager.class).setCurrentNetworkView(netView);
        }
        return netView;
    }

    public static TaskIterator getLayoutTask(TaskMonitor monitor, Manager manager, CyNetworkView networkView) {
        monitor.showMessage(TaskMonitor.Level.INFO, "Force layout application");
        CyLayoutAlgorithmManager layoutManager = manager.utils.getService(CyLayoutAlgorithmManager.class);
        CyLayoutAlgorithm alg = layoutManager.getLayout("force-directed-cl");
        if (alg == null) alg = layoutManager.getLayout("force-directed");
        Object context = alg.getDefaultLayoutContext();
        TunableSetter setter = manager.utils.getService(TunableSetter.class);
        Map<String, Object> layoutArgs = new HashMap<>();
        layoutArgs.put("defaultNodeMass", 10.0);
        setter.applyTunables(context, layoutArgs);
        Set<View<CyNode>> nodeViews = new HashSet<>(networkView.getNodeViews());
        TaskIterator taskIterator = alg.createTaskIterator(networkView, context, nodeViews, null);
        return taskIterator;
    }
}
