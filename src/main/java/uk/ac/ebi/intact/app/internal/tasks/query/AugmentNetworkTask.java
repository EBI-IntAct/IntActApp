package uk.ac.ebi.intact.app.internal.tasks.query;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.*;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;

import java.util.*;

public class AugmentNetworkTask extends AbstractTask {
    final Network network;
    final List<String> intactAcs;
    @Tunable(description = "Re-layout network?")
    public boolean relayout = false;

    public AugmentNetworkTask(final Network network, final List<String> intactAcs) {
        this.network = network;
        this.intactAcs = intactAcs;
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("Adding " + intactAcs.size() + " terms to network");
        Manager manager = network.getManager();

        Map<String, CyNode> idToNode = new HashMap<>();
        Map<String, String> idToName = new HashMap<>();
        network.getINodes().forEach(node -> {
            idToNode.put(node.ac, node.cyNode);
            idToName.put(node.ac, node.name);
        });

        // String url = "http://api.jensenlab.org/network?entities="+URLEncoder.encode(ids.trim())+"&score="+conf;
        Map<Object, Object> args = new HashMap<>();
        args.put("existingAcs", idToNode.keySet());
        args.put("newAcs", intactAcs);

        JsonNode results = HttpUtils.postJSON(Manager.INTACT_GRAPH_WS, args, manager);

        if (results == null) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "IntAct returned no results");
            return;
        }

        List<CyEdge> newEdges = new ArrayList<>();
        List<CyNode> newNodes = ModelUtils.augmentNetworkFromJSON(manager, network,idToNode, idToName, newEdges, results);

        if (newEdges.size() > 0 || newNodes.size() > 0) {
            monitor.setStatusMessage("Adding " + newNodes.size() + " nodes and " + newEdges.size() + " edges");
        } else {
            throw new RuntimeException("This query will not add any new nodes or edges to the existing network.");
        }


        // Get our view
        CyNetworkView cyView = null;
        Collection<CyNetworkView> views = manager.utils.getService(CyNetworkViewManager.class).getNetworkViews(network.getCyNetwork());
        for (CyNetworkView view : views) {
            if (view.getRendererId().equals("org.cytoscape.ding")) {
                cyView = view;
                break;
            }
        }

        // If we have a view, re-apply the style and layout
        if (cyView != null) {
            cyView.updateView();

            // And lay it out
            if (relayout) {
                monitor.setStatusMessage("Updating layout");
                CyLayoutAlgorithm alg = manager.utils.getService(CyLayoutAlgorithmManager.class).getLayout("force-directed");
                Object context = alg.createLayoutContext();
                TunableSetter setter = manager.utils.getService(TunableSetter.class);
                Map<String, Object> layoutArgs = new HashMap<>();
                layoutArgs.put("defaultNodeMass", 10.0);
                setter.applyTunables(context, layoutArgs);
                Set<View<CyNode>> nodeViews = new HashSet<>(cyView.getNodeViews());
                insertTasksAfterCurrentTask(alg.createTaskIterator(cyView, context, nodeViews, "score"));
            }
        }
    }

    @ProvidesTitle
    public String getTitle() {
        return "Adding Terms to Network";
    }
}
