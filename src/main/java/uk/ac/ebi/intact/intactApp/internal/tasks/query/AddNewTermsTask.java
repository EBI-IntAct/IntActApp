package uk.ac.ebi.intact.intactApp.internal.tasks.query;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.*;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ViewUtils;

import java.util.*;

public class AddNewTermsTask extends AbstractTask {
    final IntactNetwork stringNet;
    final List<String> intactAcs;
    @Tunable(description = "Re-layout network?")
    public boolean relayout = false;

    public AddNewTermsTask(final IntactNetwork iNetwork, final List<String> intactAcs) {
        this.stringNet = iNetwork;
        this.intactAcs = intactAcs;
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("Adding " + intactAcs.size() + " terms to network");
        IntactManager manager = stringNet.getManager();
        CyNetwork network = stringNet.getNetwork();

        StringBuilder ids = null;
        for (String id : intactAcs) {
            if (ids == null)
                ids = new StringBuilder(id);
            else
                ids.append("\n").append(id);
        }


        // String url = "http://api.jensenlab.org/network?entities="+URLEncoder.encode(ids.trim())+"&score="+conf;
        Map<Object, Object> args = new HashMap<>();
        args.put("existing", ModelUtils.getExisting(network).trim());

        monitor.setStatusMessage("Getting additional terms from " + IntactManager.INTACT_GRAPH_WS);

        JsonNode results = HttpUtils.postJSON(IntactManager.INTACT_GRAPH_WS, args, manager);

        if (results == null) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "String returned no results");
            return;
        }
        //TODO Network augmentation
//        monitor.setStatusMessage("Augmenting network");

        List<CyEdge> newEdges = new ArrayList<>();
        List<CyNode> newNodes = ModelUtils.augmentNetworkFromJSON(manager, network, newEdges, results);

//        if (newEdges.size() > 0 || newNodes.size() > 0) {
//            monitor.setStatusMessage("Adding " + newNodes.size() + " nodes and " + newEdges.size() + " edges");
//        } else {
//            throw new RuntimeException("This query will not add any new nodes or edges to the existing network.");
//        }


        // Get our view
        CyNetworkView netView = null;
        Collection<CyNetworkView> views =
                manager.utils.getService(CyNetworkViewManager.class).getNetworkViews(network);
        for (CyNetworkView view : views) {
            if (view.getRendererId().equals("org.cytoscape.ding")) {
                netView = view;
                break;
            }
        }

        // If we have a view, re-apply the style and layout
        if (netView != null) {
            monitor.setStatusMessage("Updating style");
            // monitor.setStatusMessage("Laying out network");
            ViewUtils.updateNodeStyle(manager, netView, newNodes);
            ViewUtils.updateEdgeStyle(manager, netView, newEdges);
            netView.updateView();

            // And lay it out
            if (relayout) {
                monitor.setStatusMessage("Updating layout");
                CyLayoutAlgorithm alg = manager.utils.getService(CyLayoutAlgorithmManager.class).getLayout("force-directed");
                Object context = alg.createLayoutContext();
                TunableSetter setter = manager.utils.getService(TunableSetter.class);
                Map<String, Object> layoutArgs = new HashMap<>();
                layoutArgs.put("defaultNodeMass", 10.0);
                setter.applyTunables(context, layoutArgs);
                Set<View<CyNode>> nodeViews = new HashSet<>(netView.getNodeViews());
                insertTasksAfterCurrentTask(alg.createTaskIterator(netView, context, nodeViews, "score"));
            }
        }
    }

    @ProvidesTitle
    public String getTitle() {
        return "Adding Terms to Network";
    }
}
