package uk.ac.ebi.intact.intactApp.internal.tasks.query;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.*;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ViewUtils;

import java.util.*;

import static uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager.INTACT_GRAPH_WS;

public class LoadInteractions extends AbstractTask {
    private final IntactNetwork intactNet;
    private final List<String> intactAcs;
    private final boolean includeNeighbours;
    private final String netName;

    public LoadInteractions(final IntactNetwork intactNet,
                            final List<String> intactAcs,
                            boolean includeNeighbours, final String netName) {
        this.intactNet = intactNet;
        this.intactAcs = intactAcs;
        this.includeNeighbours = includeNeighbours;
        this.netName = netName;
    }

    public void run(TaskMonitor monitor) {
        IntactManager manager = intactNet.getManager();
        Map<Object, Object> postData = new HashMap<>();

        postData.put("interactorAcs", intactAcs);
        System.out.println(intactAcs);
        postData.put("neighboursRequired", includeNeighbours);

        monitor.setTitle("Load IntAct Network");
        monitor.setTitle("Querying IntAct servers");
        monitor.setProgress(0.2);
        JsonNode results = HttpUtils.postJSON(INTACT_GRAPH_WS + "network/data", postData, manager);
        // This may change...
        monitor.setTitle("Parsing result data");
        monitor.setProgress(0.4);
        CyNetwork network = ModelUtils.createIntactNetworkFromJSON(intactNet, results, netName);

        if (network == null) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "IntAct returned no results");
            return;
        }

        monitor.setTitle("Create collapsed edges");
        monitor.setProgress(0.6);
        manager.data.addIntactNetwork(intactNet, network);
        manager.data.fireIntactNetworkCreated(intactNet);

        monitor.setTitle("Register network");
        monitor.setProgress(0.7);
        manager.data.addNetwork(network);

        // Now style the network
        monitor.setTitle("Create and register network view");
        monitor.setProgress(0.8);
        CyNetworkView networkView = manager.data.createNetworkView(network);
        ViewUtils.createView(manager, networkView);

        // And lay it out
        CyLayoutAlgorithm alg = manager.utils.getService(CyLayoutAlgorithmManager.class).getLayout("force-directed");
        Object context = alg.createLayoutContext();
        TunableSetter setter = manager.utils.getService(TunableSetter.class);
        Map<String, Object> layoutArgs = new HashMap<>();
        layoutArgs.put("defaultNodeMass", 10.0);
        setter.applyTunables(context, layoutArgs);
        Set<View<CyNode>> nodeViews = new HashSet<>(networkView.getNodeViews());
        insertTasksAfterCurrentTask(alg.createTaskIterator(networkView, context, nodeViews, ModelUtils.MI_SCORE));
        manager.utils.showResultsPanel();
    }

    @ProvidesTitle
    public String getTitle() {
        return "Loading interactions";
    }
}
