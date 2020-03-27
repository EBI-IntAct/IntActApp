package uk.ac.ebi.intact.intactApp.internal.tasks;

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
import uk.ac.ebi.intact.intactApp.internal.model.Databases;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ViewUtils;

import java.util.*;

public class LoadTermsTask extends AbstractTask {
    final IntactNetwork stringNet;
    final String species;
    final int taxonId;
    final int confidence;
    final int additionalNodes;
    final List<String> stringIds;
    final Map<String, String> queryTermMap;
    @Tunable(description = "Re-layout network?")
    public boolean relayout = false;
    String useDATABASE;

    public LoadTermsTask(final IntactNetwork stringNet, final String species, final int taxonId,
                         final int confidence, final int additionalNodes,
                         final List<String> stringIds,
                         final Map<String, String> queryTermMap, final String useDATABASE) {
        this.stringNet = stringNet;
        this.taxonId = taxonId;
        this.additionalNodes = additionalNodes;
        this.confidence = confidence;
        this.stringIds = stringIds;
        this.species = species;
        this.queryTermMap = queryTermMap;
        this.useDATABASE = useDATABASE;
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("Adding " + stringIds.size() + " terms to network");
        IntactManager manager = stringNet.getManager();
        CyNetwork network = stringNet.getNetwork();

        StringBuilder ids = null;
        for (String id : stringIds) {
            if (ids == null)
                ids = new StringBuilder(id);
            else
                ids.append("\n").append(id);
        }

        String conf = "0." + confidence;
        if (confidence == 100)
            conf = "1.0";

        // String url = "http://api.jensenlab.org/network?entities="+URLEncoder.encode(ids.trim())+"&score="+conf;
        Map<String, String> args = new HashMap<>();
        // args.put("database", useDATABASE);
        // TODO: Is it OK to always use stitch?
        args.put("database", Databases.STITCH.getAPIName());
        args.put("entities", ids.toString().trim());
        args.put("score", conf);
        if (additionalNodes > 0) {
            args.put("additional", Integer.toString(additionalNodes));
            if (useDATABASE.equals(Databases.STRING.getAPIName())) {
                args.put("filter", taxonId + ".%%");
            } else {
                args.put("filter", taxonId + ".%%|CIDm%%");
            }
        }
        args.put("existing", ModelUtils.getExisting(network).trim());

        monitor.setStatusMessage("Getting additional terms from " + manager.getNetworkURL());

        JsonNode results = HttpUtils.postJSON(manager.getNetworkURL(), args, manager);

        if (results == null) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "String returned no results");
            return;
        }

        monitor.setStatusMessage("Augmenting network");

        List<CyEdge> newEdges = new ArrayList<>();
        List<CyNode> newNodes = ModelUtils.augmentNetworkFromJSON(manager, network, newEdges,
                results, queryTermMap, useDATABASE);

        if (newEdges.size() > 0 || newNodes.size() > 0) {
            monitor.setStatusMessage("Adding " + newNodes.size() + " nodes and " + newEdges.size() + " edges");
        } else {
            // monitor.showMessage(Level.WARN, "Adding "+newNodes.size()+" nodes and "+newEdges.size()+" edges");
            throw new RuntimeException("This query will not add any new nodes or edges to the existing network.");
            // SwingUtilities.invokeLater(new Runnable() {
            // public void run() {
            // JOptionPane.showMessageDialog(null,
            // "This query will not add any new nodes or edges to the existing network.",
            // "Warning", JOptionPane.WARNING_MESSAGE);
            // }
            // });
            // return;
        }

        // Set our confidence score
        ModelUtils.setConfidence(network, ((double) confidence) / 100.0);

        // Get our view
        CyNetworkView netView = null;
        Collection<CyNetworkView> views =
                manager.getService(CyNetworkViewManager.class).getNetworkViews(network);
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
                CyLayoutAlgorithm alg = manager.getService(CyLayoutAlgorithmManager.class).getLayout("force-directed");
                Object context = alg.createLayoutContext();
                TunableSetter setter = manager.getService(TunableSetter.class);
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
