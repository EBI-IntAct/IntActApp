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
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields;

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

        CyLayoutAlgorithmManager layoutAlgorithmManager = manager.utils.getService(CyLayoutAlgorithmManager.class);
        TunableSetter tunableSetter = manager.utils.getService(TunableSetter.class);

        Map<String, Object> layoutArgs;
        CyLayoutAlgorithm forceLayout = layoutAlgorithmManager.getLayout("yfiles.OrganicLayout");
        Object context;

        if (forceLayout == null) {
            forceLayout = layoutAlgorithmManager.getLayout("force-directed-cl");
            if (forceLayout == null) forceLayout = layoutAlgorithmManager.getLayout("force-directed");
            context = forceLayout.getDefaultLayoutContext();
            layoutArgs = new HashMap<>();
            layoutArgs.put("defaultSpringCoefficient", 1E-5);
            layoutArgs.put("defaultSpringLength", 20);
            layoutArgs.put("defaultNodeMass", 10);

            layoutArgs.put("defaultEdgeWeight", 0.08);
            layoutArgs.put("edgeAttribute", EdgeFields.WEIGHT.toString());
            layoutArgs.put("type", EdgeFields.WEIGHT.toString());

            tunableSetter.applyTunables(context, layoutArgs);
        } else {
            context = forceLayout.getDefaultLayoutContext();
        }

        Set<View<CyNode>> nodeViews = new HashSet<>(networkView.getNodeViews());
        return forceLayout.createTaskIterator(networkView, context, nodeViews, null);
    }
}
