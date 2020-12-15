package uk.ac.ebi.intact.app.internal.tasks.query;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.*;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.*;
import org.cytoscape.work.TaskMonitor.Level;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;
import uk.ac.ebi.intact.app.internal.utils.ViewUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class CreateNetworkTask extends AbstractTask implements TaskObserver {
    private final Network network;
    private final List<String> intactAcs;
    private final boolean includeNeighbours;
    private final String netName;
    private Instant begin;

    public CreateNetworkTask(final Network network,
                             final List<String> intactAcs,
                             boolean includeNeighbours, final String netName) {
        this.network = network;
        this.intactAcs = intactAcs;
        this.includeNeighbours = includeNeighbours;
        this.netName = netName;

    }

    public void run(TaskMonitor monitor) {
        Manager manager = network.manager;
        Map<Object, Object> postData = new HashMap<>();

        postData.put("interactorAcs", intactAcs);
        System.out.println(intactAcs);
        postData.put("neighboursRequired", includeNeighbours);

        if (cancelled) return;
        monitor.setTitle("Load IntAct Network");
        monitor.setTitle("Querying IntAct servers");
        monitor.setProgress(0.2);
        monitor.showMessage(Level.INFO, "Querying IntAct servers");
        begin = Instant.now();
        manager.utils.registerService(this, TaskObserver.class, new Properties());

        JsonNode results = HttpUtils.postJSON(Manager.INTACT_GRAPH_WS + "network/data", postData, manager, () -> cancelled);
        System.out.println(Duration.between(begin, Instant.now()).toSeconds());
        // This may change...
        monitor.setTitle("Parsing result data");
        monitor.showMessage(Level.INFO, "Parsing data");
        monitor.setProgress(0.4);
        if (cancelled) return;
        CyNetwork cyNetwork = ModelUtils.createIntactNetworkFromJSON(network, results, netName, () -> cancelled);

        if (cyNetwork == null) {
            monitor.showMessage(Level.ERROR, "IntAct returned no results");
            return;
        }

        if (cancelled) {
            destroyNetwork(manager, network);
            return;
        }

        monitor.setTitle("Create summary edges");
        monitor.showMessage(Level.INFO,"Create summary edges");
        monitor.setProgress(0.6);
        manager.data.addNetwork(network, cyNetwork);
        manager.data.fireIntactNetworkCreated(network);
        System.out.println(Duration.between(begin, Instant.now()).toSeconds());

        if (cancelled) {
            destroyNetwork(manager, network);
            return;
        }

        monitor.setTitle("Register network");
        monitor.showMessage(Level.INFO, "Register network");
        monitor.setProgress(0.7);
        manager.data.setCurrentNetwork(cyNetwork);
        System.out.println(Duration.between(begin, Instant.now()).toSeconds());
        if (cancelled) {
            manager.utils.getService(CyNetworkManager.class).destroyNetwork(cyNetwork);
        }

        if (cancelled) {
            destroyNetwork(manager, network);
            return;
        }

        // Now style the network
        monitor.setTitle("Create and register network view + Initialize filters");
        monitor.showMessage(Level.INFO, "Create and register network view + Initialize filters");
        monitor.setProgress(0.8);
        CyNetworkView networkView = manager.data.createNetworkView(cyNetwork);
        ViewUtils.registerView(manager, networkView);
        System.out.println(Duration.between(begin, Instant.now()).toSeconds());

        if (cancelled) {
            destroyNetwork(manager, network);
            return;
        }

        // And lay it out
        monitor.showMessage(Level.INFO, "Force layout application");
        CyLayoutAlgorithm alg = manager.utils.getService(CyLayoutAlgorithmManager.class).getLayout("force-directed");
        Object context = alg.getDefaultLayoutContext();
        TunableSetter setter = manager.utils.getService(TunableSetter.class);
        Map<String, Object> layoutArgs = new HashMap<>();
        layoutArgs.put("defaultNodeMass", 10.0);
        setter.applyTunables(context, layoutArgs);
        Set<View<CyNode>> nodeViews = new HashSet<>(networkView.getNodeViews());
        TaskIterator taskIterator = alg.createTaskIterator(networkView, context, nodeViews, null);
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
        System.out.println("All finished : " + Duration.between(begin, Instant.now()).toSeconds());
    }

    private void destroyNetwork(Manager manager, Network network) {
        CyNetwork cyNetwork = network.getCyNetwork();

        CyNetworkManager networkManager = manager.utils.getService(CyNetworkManager.class);
        CyTableManager tableManager = manager.utils.getService(CyTableManager.class);

        if (cyNetwork != null && networkManager.networkExists(cyNetwork.getSUID()))
            networkManager.destroyNetwork(cyNetwork);

        CyTable featuresTable = network.getFeaturesTable();
        if (featuresTable != null) tableManager.deleteTable(featuresTable.getSUID());

        CyTable identifiersTable = network.getIdentifiersTable();
        if (identifiersTable != null) tableManager.deleteTable(identifiersTable.getSUID());
        manager.data.removeNetwork(network);
    }
}
