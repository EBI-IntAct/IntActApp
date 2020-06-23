package uk.ac.ebi.intact.app.internal.utils;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;

import java.util.List;

public class ViewUtils {
    public static String STYLE_NAME_NAMESPACES = "STRING style v1.5";


    public static CyNetworkView createView(Manager manager, CyNetworkView netView) {
        if (netView != null) {
            manager.utils.getService(CyNetworkViewManager.class).addNetworkView(netView);
            manager.utils.getService(CyApplicationManager.class).setCurrentNetworkView(netView);
        }
        return netView;
    }

    public static void updateNodeStyle(Manager manager,
                                       CyNetworkView cyView, List<CyNode> nodes) {
        // manager.flushEvents();
        VisualMappingManager vmm = manager.utils.getService(VisualMappingManager.class);
        VisualStyle style = vmm.getVisualStyle(cyView);
        for (CyNode node : nodes) {
            if (cyView.getNodeView(node) != null)
                style.apply(cyView.getModel().getRow(node), cyView.getNodeView(node));
        }
        // style.apply(cyView);
    }

    public static void updateEdgeStyle(Manager manager, CyNetworkView cyView, List<CyEdge> edges) {
        // manager.flushEvents();
        VisualMappingManager vmm = manager.utils.getService(VisualMappingManager.class);
        VisualStyle style = vmm.getVisualStyle(cyView);
        for (CyEdge edge : edges) {
            if (cyView.getEdgeView(edge) != null)
                style.apply(cyView.getModel().getRow(edge), cyView.getEdgeView(edge));
        }
        // style.apply(cyView);
    }


}
