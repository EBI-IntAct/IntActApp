package uk.ac.ebi.intact.app.internal.tasks.view.extract;

import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NetworkFields;
import uk.ac.ebi.intact.app.internal.tasks.view.AbstractViewTask;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;
import uk.ac.ebi.intact.app.internal.utils.ViewUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExtractNetworkViewTask extends AbstractViewTask {
    @Tunable(description = "Include filtered elements.<br> Default = false", dependsOn = "view!=")
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
        NetworkFields.EXPORTED.setValue(newNetwork.getRow(newNetwork), true);
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

        copyColumns(srcNodeTable, newNodeTable);
        copyColumns(srcEdgeTable, newEdgeTable);
        copyColumns(srcNetworkTable, newNetworkTable);
    }

    private void copyColumns(CyTable srcTable, CyTable newTable) {
        srcTable.getColumns().forEach(column -> {
            Class<?> elementType = column.getListElementType();
            if (newTable.getColumn(column.getName()) != null) return;
            if (elementType == null) {
                newTable.createColumn(column.getName(), column.getType(), column.isImmutable(), column.getDefaultValue());
            } else {
                newTable.createListColumn(column.getName(), elementType, column.isImmutable());
            }
        });
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
            insertTasksAfterCurrentTask(ViewUtils.getLayoutTask(monitor, manager, networkView));
        }
    }
}
