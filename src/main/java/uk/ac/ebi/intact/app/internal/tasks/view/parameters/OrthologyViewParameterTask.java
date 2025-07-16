package uk.ac.ebi.intact.app.internal.tasks.view.parameters;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NodeFields;
import uk.ac.ebi.intact.app.internal.tasks.view.AbstractViewTask;
import uk.ac.ebi.intact.app.internal.utils.ViewUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OrthologyViewParameterTask extends AbstractViewTask {
    public static final int CIRCLE_LAYOUT_SPACING = 90;
    private final String DEFAULT_ORTHOLOGY_DB = "panther";
    private final boolean isParameterApplied;
    private NetworkView networkView;

    public OrthologyViewParameterTask(Manager manager, NetworkView networkView, boolean isParameterApplied) {
        super(manager, networkView);
        this.networkView = networkView;
        this.isParameterApplied = isParameterApplied;
    }

    public OrthologyViewParameterTask(Manager manager, boolean currentView) {
        super(manager, currentView);
        this.isParameterApplied = true;
    }

    @Override
    public void run(TaskMonitor monitor) throws Exception {
        chooseData();
        manager.data.viewParameterChanged(chosenView);
        if (isParameterApplied) {
            chosenNetwork.collapseGroups(NodeFields.ORTHOLOG_GROUP_ID, DEFAULT_ORTHOLOGY_DB);
            applyParameterLayout(monitor);
        } else {
            chosenNetwork.expandGroups();
            resetLayout(monitor);
        }
    }

    private void applyParameterLayout(TaskMonitor monitor) throws InterruptedException {
        SynchronousTaskManager<?> taskManager = manager.utils.getService(SynchronousTaskManager.class);
        CyNetworkFactory networkFactory = manager.utils.getService(CyNetworkFactory.class);
        CyNetworkManager networkManager = manager.utils.getService(CyNetworkManager.class);
        CyNetworkViewFactory networkViewFactory = manager.utils.getService(CyNetworkViewFactory.class);
        CyNetworkViewManager networkViewManager = manager.utils.getService(CyNetworkViewManager.class);

        // Step 0: Initialise temp network
        CyGroupManager groupManager = chosenNetwork.getGroupManager();
        Set<CyGroup> groupSet = groupManager.getGroupSet(chosenNetwork.getCyNetwork());
        CyNetwork tempNetwork = networkFactory.createNetworkWithPrivateTables();
        networkManager.addNetwork(tempNetwork, false);
        CyNetworkView tempView = networkViewFactory.createNetworkView(tempNetwork);
        networkViewManager.addNetworkView(tempView, false);

        Map<CyNode, CyNode> childNodesToCompoundNode = groupSet.stream()
                .flatMap(group -> group.getNodeList()
                        .stream()
                        .map(childNode -> new ImmutablePair<>(childNode, group.getGroupNode()))
                )
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
        Map<CyNode, List<CyNode>> compoundNodeToChildNodes = groupSet.stream()
                .collect(Collectors.toMap(CyGroup::getGroupNode, CyGroup::getNodeList));

        Map<CyNode, CyNode> originalToCopyNode = new HashMap<>();
        Map<CyNode, CyNode> copyToOriginalNode = new HashMap<>();


        // Step 1: layout groups using simple circle layout
        groupSet.stream().parallel().forEach(group -> {
            List<CyNode> childNodes = group.getNodeList();
            int n = childNodes.size();
            double circumference = n * CIRCLE_LAYOUT_SPACING;
            double radius = circumference / (2 * Math.PI);

            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n;
                View<CyNode> nodeView = cyView.getNodeView(childNodes.get(i));
                nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, radius * Math.cos(angle));
                nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, radius * Math.sin(angle));
            }
        });
        cyView.updateView();

        // Step 2: create a clone graph with the compound nodes and the singletons, with correct node size
        chosenNetwork.getCyNetwork().getNodeList().stream()
                .filter(node -> !childNodesToCompoundNode.containsKey(node)) // Remove all nested nodes as their relative position is already given by the grid layout
                .forEach(originalNode -> {
                    CyNode copyNode = tempNetwork.addNode();
                    originalToCopyNode.put(originalNode, copyNode);
                    copyToOriginalNode.put(copyNode, originalNode);
                    tempView.updateView();
                    View<CyNode> originalNodeView = cyView.getNodeView(originalNode);
                    View<CyNode> copyNodeView = tempView.getNodeView(copyNode);
                    copyNodeView.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, originalNodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH));
                    copyNodeView.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, originalNodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT));
                    copyNodeView.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.RECTANGLE);
                });
        chosenNetwork.getCyNetwork().getEdgeList().forEach(edge -> {
            CyNode source = childNodesToCompoundNode.getOrDefault(edge.getSource(), edge.getSource());
            CyNode target = childNodesToCompoundNode.getOrDefault(edge.getTarget(), edge.getTarget());
            source = originalToCopyNode.get(source);
            target = originalToCopyNode.get(target);
            tempNetwork.addEdge(source, target, false);
        });

        Thread.sleep(500); // Required for next layout algorithm to be running correctly somehow

        // Step 3: run force-directed layout on fake network
        taskManager.execute(ViewUtils.getLayoutTask(monitor, manager, tempView));


        // Step 4: move nodes in real network based on their position in the fake one
        tempNetwork.getNodeList().forEach(copyNode -> {
            View<CyNode> copyNodeView = tempView.getNodeView(copyNode);
            CyNode originalNode = copyToOriginalNode.get(copyNode);
            double oldX = cyView.getNodeView(originalNode).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
            double oldY = cyView.getNodeView(originalNode).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);

            double newX = copyNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
            double newY = copyNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);

            double dX = newX - oldX;
            double dY = newY - oldY;

            compoundNodeToChildNodes.getOrDefault(originalNode, List.of(originalNode)).forEach(realNode -> {
                View<CyNode> realNodeView = cyView.getNodeView(realNode);
                double initX = realNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
                double initY = realNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
                realNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, initX + dX);
                realNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, initY + dY);
            });
        });
        cyView.updateView();
        cyView.fitContent();


        // Step 5: Clean the temp network
        networkViewManager.destroyNetworkView(tempView);
        networkManager.destroyNetwork(tempNetwork);
    }

    private void resetLayout(TaskMonitor monitor) {
        manager.utils.getService(TaskManager.class).execute(ViewUtils.getLayoutTask(monitor, manager, cyView));
        cyView.updateView();
    }
}