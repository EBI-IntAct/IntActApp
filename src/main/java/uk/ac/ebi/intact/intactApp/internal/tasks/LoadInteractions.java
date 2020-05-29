package uk.ac.ebi.intact.intactApp.internal.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ViewUtils;

import java.util.*;

import static uk.ac.ebi.intact.intactApp.internal.model.IntactManager.INTACT_ENDPOINT_URL;

public class LoadInteractions extends AbstractTask {
    final IntactNetwork intactNet;
    final String species;
    final int taxonId;
    final int confidence;
    final int additionalNodes;
    final List<String> intactIds;
    final Map<String, String> queryTermMap;
    final String netName;
    final String useDATABASE;

    public LoadInteractions(final IntactNetwork intactNet, final String species, final int taxonId,
                            final int confidence, final int additionalNodes,
                            final List<String> intactIds,
                            final Map<String, String> queryTermMap,
                            final String netName,
                            final String useDATABASE) {
        this.intactNet = intactNet;
        this.taxonId = taxonId;
        this.additionalNodes = additionalNodes;
        this.confidence = confidence;
        this.intactIds = intactIds;
        this.species = species;
        this.queryTermMap = queryTermMap;
        this.netName = netName;
        this.useDATABASE = useDATABASE;
    }

    public void run(TaskMonitor monitor) {
        IntactManager manager = intactNet.getManager();

        // String url = "http://api.jensenlab.org/network?entities="+URLEncoder.encode(ids.trim())+"&score="+conf;
        Map<Object, Object> args = new HashMap<>();
        args.put("identifiers", intactIds);
//        args.put("species", 9606);

        monitor.setTitle("Querying IntAct servers");
        monitor.setProgress(0.2);
        JsonNode results = HttpUtils.postJSON(INTACT_ENDPOINT_URL + "/network/data", args, manager);
//        JsonNode results = HttpUtils.postJSON("http://localhost:8083/intact/ws/graph/interaction/cytoscape", args, manager);
        // This may change...
        monitor.setTitle("Parsing result data");
        monitor.setProgress(0.4);
        CyNetwork network = ModelUtils.createIntactNetworkFromJSON(intactNet, results, queryTermMap, netName);

        if (network == null) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "String returned no results");
            return;
        }

        monitor.setTitle("Create collapsed edges");
        monitor.setProgress(0.6);
        manager.addIntactNetwork(intactNet, network);

        monitor.setTitle("Register network");
        monitor.setProgress(0.7);
        manager.addNetwork(network);

        // System.out.println("Results: "+results.toString());
        // Now style the network
        monitor.setTitle("Create and register network view");
        monitor.setProgress(0.8);
        CyNetworkView networkView = manager.createNetworkView(network);
        ViewUtils.styleNetwork(manager, network, networkView);

        // And lay it out
        CyLayoutAlgorithm alg = manager.getService(CyLayoutAlgorithmManager.class).getLayout("force-directed");
        Object context = alg.createLayoutContext();
        TunableSetter setter = manager.getService(TunableSetter.class);
        Map<String, Object> layoutArgs = new HashMap<>();
        layoutArgs.put("defaultNodeMass", 10.0);
        setter.applyTunables(context, layoutArgs);
        Set<View<CyNode>> nodeViews = new HashSet<>(networkView.getNodeViews());
        insertTasksAfterCurrentTask(alg.createTaskIterator(networkView, context, nodeViews, ModelUtils.MI_SCORE));
        manager.showResultsPanel();
    }

    @ProvidesTitle
    public String getTitle() {
        return "Loading interactions";
    }
}
