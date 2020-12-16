package uk.ac.ebi.intact.app.internal.tasks.view.export;

import org.cytoscape.model.*;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NetworkFields;
import uk.ac.ebi.intact.app.internal.tasks.view.AbstractViewTask;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;
import uk.ac.ebi.intact.app.internal.utils.ViewUtils;

import java.util.*;

public class ExtractNetworkViewTask extends AbstractViewTask {
    @Tunable(description = "Include filtered elements", dependsOn = "view!=")
    public boolean includeFiltered = false;

    @Tunable(description = "Apply layout", dependsOn = "view!=")
    public boolean applyLayout = true;
    private CyTable newNodeTable;
    private CyTable newEdgeTable;
    private CyTable newNetworkTable;
    private CyTable srcNodeTable;
    private CyTable srcEdgeTable;
    private CyTable srcNetworkTable;
    private CyNetwork newNetwork;
    private Collection<Node> nodesToExport;
    private Collection<? extends Edge> edgesToExport;

    public ExtractNetworkViewTask(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    public ExtractNetworkViewTask(Manager manager, NetworkView view) {
        super(manager, view);
    }

    @Override
    public void run(TaskMonitor monitor) {
        chooseData();
        if (chosenView == null || chosenNetwork == null) return;
        collectSourceTables();
        initNewNetwork();

        if (includeFiltered) {
            nodesToExport = chosenNetwork.getNodes();
            edgesToExport = chosenView.getType().equals(NetworkView.Type.SUMMARY) ? chosenNetwork.getSummaryEdges() : chosenNetwork.getEvidenceEdges();
        } else {
            nodesToExport = chosenView.visibleNodes;
            edgesToExport = chosenView.visibleEdges;
        }

        copyData();
        createView(monitor);
    }

    private void collectSourceTables() {
        srcNodeTable = chosenNetwork.getCyNetwork().getDefaultNodeTable();
        srcEdgeTable = chosenNetwork.getCyNetwork().getDefaultEdgeTable();
        srcNetworkTable = chosenNetwork.getCyNetwork().getDefaultNetworkTable();
    }

    private void initNewNetwork() {
        newNetwork = manager.data.createNetwork(String.format("Export of %s's %s view ", chosenNetwork.toString(), chosenView.getType().toString()));
        manager.utils.getService(CyNetworkManager.class).addNetwork(newNetwork);

        newNodeTable = newNetwork.getDefaultNodeTable();
        newEdgeTable = newNetwork.getDefaultEdgeTable();
        newNetworkTable = newNetwork.getDefaultNetworkTable();

        Table.NODE.initColumns(newNodeTable, newNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS));
        Table.EDGE.initColumns(newEdgeTable, newNetwork.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS));
        Table.NETWORK.initColumns(newNetworkTable, newNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS));

        NetworkFields.EXPORTED.setValue(newNetwork.getRow(newNetwork), true);
    }

    private void copyData() {
        Map<Node, CyNode> srcToNewNodes = new HashMap<>();

        nodesToExport.forEach(srcNode -> {
            CyNode newNode = newNetwork.addNode();
            TableUtil.copyRow(srcNodeTable, newNodeTable, srcNode.cyNode.getSUID(), newNode.getSUID(), Set.of());
            srcToNewNodes.put(srcNode, newNode);
        });

        edgesToExport.forEach(srcEdge -> {
            CyEdge newEdge = newNetwork.addEdge(srcToNewNodes.get(srcEdge.source), srcToNewNodes.get(srcEdge.target), srcEdge.cyEdge.isDirected());
            TableUtil.copyRow(srcEdgeTable, newEdgeTable, srcEdge.cyEdge.getSUID(), newEdge.getSUID(), Set.of());
        });

        TableUtil.copyRow(srcNetworkTable, newNetworkTable, chosenNetwork.getCyNetwork().getSUID(), newNetwork.getSUID(), Set.of(NetworkFields.UUID.name));
    }

    private void createView(TaskMonitor monitor) {
        CyNetworkView networkView = manager.utils.getService(CyNetworkViewFactory.class).createNetworkView(newNetwork);
        manager.style.getStyle(chosenView.getType()).applyStyle(networkView);
        ViewUtils.registerView(manager, networkView);

        applyLayout(monitor, networkView);
    }

    private void applyLayout(TaskMonitor monitor, CyNetworkView networkView) {
        if (applyLayout) {
            monitor.showMessage(TaskMonitor.Level.INFO, "Force layout application");
            CyLayoutAlgorithm alg = manager.utils.getService(CyLayoutAlgorithmManager.class).getLayout("force-directed");
            Object context = alg.getDefaultLayoutContext();
            TunableSetter setter = manager.utils.getService(TunableSetter.class);
            Map<String, Object> layoutArgs = new HashMap<>();
            layoutArgs.put("defaultNodeMass", 10.0);
            setter.applyTunables(context, layoutArgs);
            Set<View<CyNode>> nodeViews = new HashSet<>(networkView.getNodeViews());
            TaskIterator taskIterator = alg.createTaskIterator(networkView, context, nodeViews, null);
            insertTasksAfterCurrentTask(taskIterator);
        }
    }
}
