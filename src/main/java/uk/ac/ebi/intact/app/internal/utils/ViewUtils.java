package uk.ac.ebi.intact.app.internal.utils;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;

import java.util.List;

public class ViewUtils {
    public static String STYLE_NAME_NAMESPACES = "STRING style v1.5";


    public static CyNetworkView createView(IntactManager manager, CyNetworkView netView) {
        if (netView != null) {
            manager.utils.getService(CyNetworkViewManager.class).addNetworkView(netView);
            manager.utils.getService(CyApplicationManager.class).setCurrentNetworkView(netView);
        }
        return netView;
    }

    public static void updateNodeStyle(IntactManager manager,
                                       CyNetworkView view, List<CyNode> nodes) {
        // manager.flushEvents();
        VisualMappingManager vmm = manager.utils.getService(VisualMappingManager.class);
        VisualStyle style = vmm.getVisualStyle(view);
        for (CyNode node : nodes) {
            if (view.getNodeView(node) != null)
                style.apply(view.getModel().getRow(node), view.getNodeView(node));
        }
        // style.apply(view);
    }

    public static void updateEdgeStyle(IntactManager manager, CyNetworkView view, List<CyEdge> edges) {
        // manager.flushEvents();
        VisualMappingManager vmm = manager.utils.getService(VisualMappingManager.class);
        VisualStyle style = vmm.getVisualStyle(view);
        for (CyEdge edge : edges) {
            if (view.getEdgeView(edge) != null)
                style.apply(view.getModel().getRow(edge), view.getEdgeView(edge));
        }
        // style.apply(view);
    }


}
