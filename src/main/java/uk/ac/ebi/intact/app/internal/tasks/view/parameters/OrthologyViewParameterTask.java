package uk.ac.ebi.intact.app.internal.tasks.view.parameters;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NodeFields;
import uk.ac.ebi.intact.app.internal.tasks.view.AbstractViewTask;
import uk.ac.ebi.intact.app.internal.utils.ViewUtils;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrthologyViewParameterTask extends AbstractViewTask {
    public static final int CIRCLE_LAYOUT_SPACING = 90;
    public static final String DEFAULT_ORTHOLOGY_DB = "panther";
    private static boolean running;
    private final boolean isParameterApplied;
    private final String database;

    public OrthologyViewParameterTask(Manager manager, NetworkView networkView, boolean isParameterApplied, String database) {
        super(manager, networkView);
        this.isParameterApplied = isParameterApplied;
        this.database = database;
    }

    public OrthologyViewParameterTask(Manager manager, boolean currentView) {
        super(manager, currentView);
        this.isParameterApplied = true;
        this.database = DEFAULT_ORTHOLOGY_DB;
    }

    @Override
    public void run(TaskMonitor monitor) throws Exception {
        chooseData();
        manager.data.viewParameterChanged(chosenView);
        if (running) return;
        running = true;
        if (isParameterApplied) {
            Map<String, List<CyNode>> groups = chosenNetwork.groupNodesByProperty(NodeFields.ORTHOLOG_GROUP_ID, database);
            if (groups.isEmpty()) {
                resetLayout(monitor);
            } else {
                chosenNetwork.collapseGroups(groups);
                chosenView.filter(); // Need to filter after collapsing as collapsing set the child node visibility to true
                applyParameterLayout(monitor);
            }
        } else {
            chosenNetwork.expandGroups();
            resetLayout(monitor);
        }
        running = false;
    }

    private void applyParameterLayout(TaskMonitor monitor) {
        CyNetworkFactory networkFactory = manager.utils.getService(CyNetworkFactory.class);
        CyNetworkManager networkManager = manager.utils.getService(CyNetworkManager.class);
        CyNetworkViewFactory networkViewFactory = manager.utils.getService(CyNetworkViewFactory.class);
        CyNetworkViewManager networkViewManager = manager.utils.getService(CyNetworkViewManager.class);
        CyEventHelper eventHelper = manager.utils.getService(CyEventHelper.class);

        // Step 0: Initialise temp network
        CyGroupManager groupManager = chosenNetwork.getGroupManager();
        Set<CyGroup> groupSet = groupManager.getGroupSet(chosenNetwork.getCyNetwork());
        CyNetwork tempNetwork = networkFactory.createNetworkWithPrivateTables();
        networkManager.addNetwork(tempNetwork, false);
        CyNetworkView tempView = networkViewFactory.createNetworkView(tempNetwork);
        networkViewManager.addNetworkView(tempView, false);

        Map<CyNode, List<CyNode>> compoundNodeToChildNodes = groupSet.stream()
                .collect(Collectors.toMap(CyGroup::getGroupNode, CyGroup::getNodeList));

        Map<CyNode, CyNode> childNodesToCompoundNode = compoundNodeToChildNodes.entrySet().stream()
                .flatMap(e -> e.getValue()
                        .stream()
                        .map(childNode -> new ImmutablePair<>(childNode, e.getKey()))
                )
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));


        Map<CyNode, CyNode> originalToCopyNode = new HashMap<>();
        Map<CyNode, CyNode> copyToOriginalNode = new HashMap<>();

        try {
            // Step 1: layout groups using simple circle layout
            groupSet.stream().parallel().forEach(this::simpleLayoutGroup);
            cyView.updateView();

            // Step 2: create a clone graph with the compound nodes and the singletons, with correct node size
            chosenNetwork.getCyNetwork().getNodeList().stream()
                    .filter(node -> cyView.getNodeView(node).getVisualProperty(BasicVisualLexicon.NODE_VISIBLE)) // Only clone visible nodes
                    .filter(node -> !childNodesToCompoundNode.containsKey(node)) // Remove all nested nodes to get a flat network, computable by force-layout
                    .forEach(originalNode -> {
                        CyNode copyNode = tempNetwork.addNode();
                        originalToCopyNode.put(originalNode, copyNode);
                        copyToOriginalNode.put(copyNode, originalNode);
                        tempView.updateView();
                        View<CyNode> originalNodeView = cyView.getNodeView(originalNode);
                        View<CyNode> copyNodeView = tempView.getNodeView(copyNode);
                        if (copyNodeView == null || originalNodeView == null) { // This shouldn't happen but still does sometimes and create bugs
                            monitor.setStatusMessage("Error while grouping, please retry");
                            return;
                        }
                        copyNodeView.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, originalNodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH));
                        copyNodeView.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, originalNodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT));
                        copyNodeView.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.RECTANGLE);
                    });

            chosenNetwork.getCyNetwork().getEdgeList().forEach(edge -> {
                if (!cyView.getEdgeView(edge).getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE))
                    return; // Remove filtered edges
                CyNode source = childNodesToCompoundNode.getOrDefault(edge.getSource(), edge.getSource());
                CyNode target = childNodesToCompoundNode.getOrDefault(edge.getTarget(), edge.getTarget());
                source = originalToCopyNode.get(source);
                target = originalToCopyNode.get(target);
                if (source == null || target == null) return;
                tempNetwork.addEdge(source, target, false);
            });

            // Required for next layout algorithm to be running correctly somehow
            eventHelper.flushPayloadEvents();
            Thread.sleep(1000);

            // Step 3: run force-directed layout on fake network
            manager.utils.execute(ViewUtils.getLayoutTask(monitor, manager, tempView), true);

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

            // Optional step: rearrange groups to place node closer to their neighbors
            groupSet.stream().parallel().forEach(this::rearrangeGroup);
            cyView.updateView();
            cyView.fitContent();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Step 5: Clean the temp network
            networkViewManager.destroyNetworkView(tempView);
            networkManager.destroyNetwork(tempNetwork);
        }
    }

    private void simpleLayoutGroup(CyGroup group) {
        List<View<CyNode>> childNodeViews = group.getNodeList().stream().map(cyView::getNodeView).collect(Collectors.toList());
        List<View<CyNode>> visibleChildren = childNodeViews.stream().filter(n -> n.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE)).collect(Collectors.toList());
        List<View<CyNode>> hiddenChildren = childNodeViews.stream().filter(n -> !n.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE)).collect(Collectors.toList());

        layoutInCircle(visibleChildren, new Point2D.Double(0, 0));
        // Place the invisible nodes in the middle of the circle to avoid them having an impact on the group size
        layoutAtOrigin(hiddenChildren);
    }

    private static void layoutAtOrigin(List<View<CyNode>> hiddenChildren) {
        hiddenChildren.forEach(nodeView -> {
            nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, 0.0);
            nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, 0.0);
        });
    }

    private static void layoutInCircle(List<View<CyNode>> visibleChildren, Point2D.Double center) {
        int n = visibleChildren.size();
        double circumference = n * CIRCLE_LAYOUT_SPACING;
        double radius = circumference / (2 * Math.PI);

        // Arrange visible nodes in a circle
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            View<CyNode> nodeView = visibleChildren.get(i);
            nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, center.x + radius * Math.cos(angle));
            nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, center.y + radius * Math.sin(angle));
        }
    }

    private void rearrangeGroup(CyGroup group) {
        CyNetwork cyNetwork = chosenNetwork.getCyNetwork();
        Point2D.Double origin = toPoint(cyView.getNodeView(group.getGroupNode()));
        HashSet<CyNode> nodeSet = new HashSet<>(group.getNodeList());
        Map<CyNode, Double> nodeToAngle = group.getNodeList().stream()
                .filter(node -> cyView.getNodeView(node).getVisualProperty(BasicVisualLexicon.NODE_VISIBLE))
                .collect(Collectors.toMap(Function.identity(), node -> {
                    List<CyEdge> adjacentSummaryEdges = cyNetwork.getAdjacentEdgeList(node, CyEdge.Type.ANY).stream()
                            .filter(edge -> EdgeFields.IS_SUMMARY.getValue(cyNetwork.getRow(edge)))
                            .filter(edge -> {
                                CyNode neighbor = edge.getTarget() == node ? edge.getSource() : edge.getTarget();
                                return !nodeSet.contains(neighbor) && // Exclude other nodes of the group
                                        cyView.getNodeView(neighbor).getVisualProperty(BasicVisualLexicon.NODE_VISIBLE); // Exclude invisible nodes
                            })
                            .collect(Collectors.toList());
                    Point2D.Double centroid = getWeightedCentroid(node, adjacentSummaryEdges, edge -> EdgeFields.MI_SCORE.getValue(cyNetwork.getRow(edge)));
                    return getAngle(centroid, origin);
                }));
        List<View<CyNode>> nodesSortedByAngle = nodeToAngle.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .map(Map.Entry::getKey)
                .map(cyView::getNodeView)
                .collect(Collectors.toList());
        layoutInCircle(nodesSortedByAngle, origin);
    }

    private double getAngle(Point2D.Double origin, Point2D.Double target) {
        return (
                Math.toDegrees(
                        Math.atan2(target.getY() - origin.getY(), target.getX() - origin.getX()) // Gradient angle [-π, π]
                ) + 180) % 360; // Normalized to [0, 360]
    }

    private Point2D.Double getWeightedCentroid(CyNode origin, Collection<CyEdge> edges, Function<CyEdge, Double> edgeWeigher) {
        if (edges.isEmpty()) return new Point2D.Double(0, 0);
        double x = 0.0, y = 0.0, weight = 0.0;
        for (CyEdge edge : edges) {
            CyNode node = edge.getTarget() == origin ? edge.getSource() : edge.getTarget();
            Double edgeWeight = edgeWeigher.apply(edge);
            Point2D.Double point = toPoint(cyView.getNodeView(node));
            x += point.x * edgeWeight;
            y += point.y * edgeWeight;
            weight += edgeWeight;
        }
        return new Point2D.Double(x / weight, y / weight);
    }

    private static Point2D.Double toPoint(View<CyNode> view) {
        return new Point2D.Double(
                view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
                view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)
        );
    }

    private void resetLayout(TaskMonitor monitor) {
        manager.utils.getService(TaskManager.class).execute(ViewUtils.getLayoutTask(monitor, manager, cyView));
        cyView.updateView();
    }
}