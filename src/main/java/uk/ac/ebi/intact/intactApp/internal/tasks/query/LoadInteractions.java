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

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager.INTACT_GRAPH_WS;

public class LoadInteractions extends AbstractTask implements TaskObserver {
    private final IntactNetwork intactNet;
    private final List<String> intactAcs;
    private final boolean includeNeighbours;
    private final String netName;
    private Instant begin;

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
        begin = Instant.now();
        manager.utils.registerService(this, TaskObserver.class, new Properties());

        JsonNode results = HttpUtils.postJSON(INTACT_GRAPH_WS + "network/data", postData, manager);
        System.out.println(Duration.between(begin, Instant.now()).toSeconds());
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
        System.out.println(Duration.between(begin, Instant.now()).toSeconds());


        monitor.setTitle("Register network");
        monitor.setProgress(0.7);
        manager.data.addNetwork(network);
        System.out.println(Duration.between(begin, Instant.now()).toSeconds());


        // Now style the network
        monitor.setTitle("Create and register network view");
        monitor.setProgress(0.8);
        CyNetworkView networkView = manager.data.createNetworkView(network);
        ViewUtils.createView(manager, networkView);
        System.out.println(Duration.between(begin, Instant.now()).toSeconds());

        // And lay it out
        CyLayoutAlgorithm alg = manager.utils.getService(CyLayoutAlgorithmManager.class).getLayout("force-directed");
        Object context = alg.createLayoutContext();
        TunableSetter setter = manager.utils.getService(TunableSetter.class);
        Map<String, Object> layoutArgs = new HashMap<>();
        layoutArgs.put("defaultNodeMass", 10.0);
        setter.applyTunables(context, layoutArgs);
        Set<View<CyNode>> nodeViews = new HashSet<>(networkView.getNodeViews());
        TaskIterator taskIterator = alg.createTaskIterator(networkView, context, nodeViews, ModelUtils.MI_SCORE);
        insertTasksAfterCurrentTask(taskIterator);

        manager.utils.showResultsPanel();
        System.out.println(Duration.between(begin, Instant.now()).toSeconds());
    }

    @ProvidesTitle
    public String getTitle() {
        return "Loading interactions";
    }

    /**
     * Called by an <code>ObservableTask</code> when it is finished executing.
     *
     * @param task The task being observed
     */
    @Override
    public void taskFinished(ObservableTask task) {

    }

    /**
     * Called by a <code>TaskManager</code> to tell us that the task iterator has completed.
     *
     * @param finishStatus Indicates how the task iterator completed.
     */
    @Override
    public void allFinished(FinishStatus finishStatus) {
        System.out.println(Duration.between(begin, Instant.now()).toSeconds());
    }
}
