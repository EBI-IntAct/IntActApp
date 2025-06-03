package uk.ac.ebi.intact.app.internal.tasks.query;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.*;

import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;
import uk.ac.ebi.intact.app.internal.utils.ViewUtils;

import java.io.IOException;
import java.util.*;

import static uk.ac.ebi.intact.app.internal.utils.ViewUtils.getLayoutTask;

public class AdvancedSearchTask extends AbstractTask implements TaskObserver {
    private final String query;
    private final Manager manager;
    private final boolean applyLayout;
    private final Network network;
    private String netName = null;

    public AdvancedSearchTask(Manager manager, String query, boolean applyLayout) {
        this.query = query;
        this.manager = manager;
        this.network = new Network(manager);
        this.applyLayout = applyLayout;
    }

    public AdvancedSearchTask(Manager manager, String query, boolean applyLayout, String netName) {
        this(manager, query, applyLayout);
        this.netName = netName;
    }

    @Override
    public void run(TaskMonitor monitor) throws Exception {
        Manager manager = network.manager;
        this.setNetworkFromGraphApi(monitor);

        if (cancelled) return;
        monitor.setTitle("Load IntAct Network");
        monitor.setTitle("Querying IntAct servers");
        monitor.setProgress(0.2);
        monitor.showMessage(TaskMonitor.Level.INFO, "Querying IntAct servers");
        manager.utils.registerService(this, TaskObserver.class, new Properties());

        monitor.setTitle("Parsing result data");
        monitor.showMessage(TaskMonitor.Level.INFO, "Parsing data");
        monitor.setProgress(0.4);
        if (cancelled) return;

        CyNetwork cyNetwork = network.getCyNetwork();

        monitor.setTitle("Create summary edges");
        monitor.showMessage(TaskMonitor.Level.INFO, "Create summary edges");
        monitor.setProgress(0.6);

        if (cancelled) {
            destroyNetwork(manager, network);
            return;
        }

        monitor.setTitle("Register network");
        monitor.showMessage(TaskMonitor.Level.INFO, "Register network");
        monitor.setProgress(0.7);
        if (cancelled) {
            manager.utils.getService(CyNetworkManager.class).destroyNetwork(cyNetwork);
            destroyNetwork(manager, network);
            return;
        }

        monitor.setTitle("Create and register network view + Initialize filters");
        monitor.showMessage(TaskMonitor.Level.INFO, "Create and register network view + Initialize filters");
        monitor.setProgress(0.8);
        CyNetworkView networkView = manager.data.createNetworkView(cyNetwork);
        ViewUtils.registerView(manager, networkView);

        if (cancelled) {
            destroyNetwork(manager, network);
            return;
        }

        if (applyLayout) {
            TaskIterator taskIterator = getLayoutTask(monitor, manager, networkView);
            insertTasksAfterCurrentTask(taskIterator);
        }
        manager.utils.showResultsPanel();
    }

    private void setNetworkFromGraphApi(TaskMonitor monitor) {
        JsonNode fetchedNetwork;

        monitor.setTitle("Fetch network from IntAct servers");
        monitor.setProgress(0);

        ObjectMapper mapper = new ObjectMapper();


        Map<String, JsonNode> nodes = new HashMap<>();
        ArrayNode edgesArray = mapper.createArrayNode();

        try {
            int page = 0;
            JsonNode pagedResult;
            do {
                pagedResult = HttpUtils.getJsonNetworkWithRequestBody(this.query, page++);
                JsonNode network = pagedResult.get("content").get(0);
                Number totalPages = pagedResult.get("totalPages").numberValue();

                monitor.showMessage(TaskMonitor.Level.INFO, "Page " + page + " / " + totalPages);
                monitor.setProgress(page / totalPages.doubleValue());
                if (cancelled) return;

                for (JsonNode node : network.get("nodes")) {
                    nodes.putIfAbsent(node.get("id").textValue(), node);
                }

                edgesArray.addAll((ArrayNode) network.get("edges"));

            } while (!pagedResult.get("last").booleanValue());

            ObjectNode combinedNetwork = mapper.createObjectNode();

            ArrayNode nodesArray = mapper.createArrayNode().addAll(nodes.values());
            nodesArray.addAll(nodes.values());
            combinedNetwork.set("nodes", nodesArray);
            combinedNetwork.set("edges", edgesArray);
            fetchedNetwork = combinedNetwork;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (cancelled) return;

        CyNetwork cyNetwork = ModelUtils.createIntactNetworkFromJSON(network, fetchedNetwork, netName != null ? netName : query, () -> cancelled);
        manager.data.addNetwork(network, cyNetwork);
        manager.data.fireIntactNetworkCreated(network);
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


    @Override
    public void taskFinished(ObservableTask task) {

    }

    @Override
    public void allFinished(FinishStatus finishStatus) {

    }
}
