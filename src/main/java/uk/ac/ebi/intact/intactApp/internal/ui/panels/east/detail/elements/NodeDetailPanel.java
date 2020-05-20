package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements;

import com.google.common.collect.Comparators;
import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeBasics;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeDetails;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeFeatures;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeSchematic;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.identifiers.NodeIdentifiers;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.*;

/**
 * Displays information about a protein taken from STRING
 *
 * @author Scooter Morris
 */
public class NodeDetailPanel extends AbstractDetailPanel {
    private JPanel nodesPanel = null;
    private CollapsablePanel selectedNodes;
    private static final int MAXIMUM_SELECTED_NODE_SHOWN = 100;
    private final EasyGBC layoutHelper = new EasyGBC();
    public volatile boolean selectionRunning;
    private final Map<String, NodePanel> nodeIdToNodePanel = new HashMap<>();

//    private final CollapseAllButton collapseAllButton = new CollapseAllButton(true, nodeIdToNodePanel.values());

    public NodeDetailPanel(final IntactManager manager) {
        super(manager, MAXIMUM_SELECTED_NODE_SHOWN, "nodes");
        init();
        revalidate();
        repaint();
    }


    private void init() {
        setLayout(new GridBagLayout());

        EasyGBC c = new EasyGBC();

        JPanel mainPanel = new JPanel();
        {
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.setBackground(backgroundColor);
            EasyGBC d = new EasyGBC();

            mainPanel.add(createNodesPanel(), d.down().anchor("north").expandHoriz());
            mainPanel.add(Box.createVerticalGlue(), d.down().expandVert());
        }
        JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        scrollPane.getVerticalScrollBar().setBlockIncrement(10);
        add(scrollPane, c.down().anchor("west").expandBoth());
    }

    private JPanel createNodesPanel() {
        nodesPanel = new JPanel();
        nodesPanel.setBackground(backgroundColor);
        nodesPanel.setLayout(new GridBagLayout());

        selectedNodes(CyTableUtil.getNodesInState(currentINetwork.getNetwork(), CyNetwork.SELECTED, true));

        nodesPanel.setAlignmentX(LEFT_ALIGNMENT);
        selectedNodes = new CollapsablePanel("Selected nodes", nodesPanel, false);
//        LinePanel headerLine = new LinePanel(backgroundColor);
//        JLabel label = new JLabel(" Selected nodes  ");
//        label.setFont(textFont.deriveFont(15f));
//        headerLine.add(label);
//        headerLine.add(collapseAllButton);
//        selectedNodes.setHeader(headerLine);
        return selectedNodes;
    }

    // Hide all nodes who's values are less than "value"
    protected void doFilter(String type) {
        CyNetworkView view = manager.getCurrentNetworkView();
        CyNetwork net = view.getModel();
        Map<String, Double> filter = filters.get(net).get(type);
        for (CyNode node : net.getNodeList()) {
            CyRow nodeRow = net.getRow(node);
            boolean show = true;
            for (String lbl : filter.keySet()) {
                Double v = nodeRow.get(type, lbl, Double.class);
                double nv = filter.get(lbl);
                if ((v == null && nv > 0) || v < nv) {
                    show = false;
                    break;
                }
            }
            View<CyNode> nv = view.getNodeView(node);
            if (nv == null) continue;
            if (show) {
                nv.clearValueLock(BasicVisualLexicon.NODE_VISIBLE);
                for (CyEdge e : net.getAdjacentEdgeList(node, CyEdge.Type.ANY)) {
                    final View<CyEdge> ev = view.getEdgeView(e);
                    if (ev == null) continue;
                    ev.clearValueLock(BasicVisualLexicon.EDGE_VISIBLE);
                }
            } else {
                nv.setLockedValue(BasicVisualLexicon.NODE_VISIBLE, false);
                net.getRow(node).set(CyNetwork.SELECTED, false);
                for (CyEdge e : net.getAdjacentEdgeList(node, CyEdge.Type.ANY)) {
                    final View<CyEdge> ev = view.getEdgeView(e);
                    if (ev == null) continue;
                    net.getRow(e).set(CyNetwork.SELECTED, false);
                    ev.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);
                }
            }
        }
    }


    private boolean checkCurrentNetwork() {
        if (currentINetwork == null) {
            currentINetwork = manager.getCurrentIntactNetwork();
            return currentINetwork != null;
        }
        return true;
    }

    public void networkChanged(IntactNetwork newNetwork) {
        this.currentINetwork = newNetwork;
        selectedNodes(newNetwork.getSelectedNodes());
    }


    public void selectedNodes(Collection<CyNode> nodes) {
        if (checkCurrentNetwork()) {
            selectionRunning = true;

            List<IntactNode> iNodes = nodes.stream()
                    .map(node -> new IntactNode(currentINetwork, node))
                    .collect(Comparators.least(MAXIMUM_SELECTED_NODE_SHOWN, nullsLast((comparing(o -> o.name, nullsLast(naturalOrder()))))));

            List<String> nodesIds = iNodes.stream().map(iNode -> iNode.id).collect(Collectors.toList());

            for (IntactNode node : iNodes) {
                if (!selectionRunning) {
                    break;
                }

                if (!nodeIdToNodePanel.containsKey(node.id)) {
                    NodePanel newPanel = new NodePanel(node);
                    newPanel.setAlignmentX(LEFT_ALIGNMENT);
                    nodesPanel.add(newPanel, layoutHelper.anchor("west").down().expandHoriz());
                    nodeIdToNodePanel.put(node.id, newPanel);
                }
            }
            if (nodes.size() < MAXIMUM_SELECTED_NODE_SHOWN) {
                nodesPanel.remove(limitExceededPanel);
            } else {
                nodesPanel.add(limitExceededPanel, layoutHelper.anchor("west").down().expandHoriz());
            }
            HashSet<String> unselectedNodes = new HashSet<>(nodeIdToNodePanel.keySet());
            unselectedNodes.removeAll(nodesIds);
            for (String unselectedNodeId : unselectedNodes) {
                NodePanel nodePanel = nodeIdToNodePanel.get(unselectedNodeId);
                nodePanel.delete();
                nodesPanel.remove(nodePanel);
                nodeIdToNodePanel.remove(unselectedNodeId);
            }

            selectionRunning = false;
        }

        revalidate();
        repaint();
    }

    private class NodePanel extends CollapsablePanel {

        private final NodeFeatures nodeFeatures;
        final NodeDetails nodeDetails;
        final IntactNode node;

        public NodePanel(IntactNode node) {
            super("", !(selectedNodes == null || selectedNodes.collapseAllButton.isExpanded()));
            this.node = node;
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setAlignmentX(LEFT_ALIGNMENT);
            setBackground(backgroundColor);
            setHeader(new NodeSchematic(node, node.getFeatures(), openBrowser));
            content.add(new NodeBasics(node, openBrowser));
            nodeFeatures = new NodeFeatures(node, node.getFeatures(), openBrowser, true, true);
            content.add(nodeFeatures);
            content.add(new NodeIdentifiers(node, openBrowser));
            nodeDetails = new NodeDetails(node, openBrowser);
            content.add(nodeDetails);
        }

        public void delete() {
            nodeFeatures.deleteEdgeSelectionCheckboxes();
        }
    }

//    private void completeNodeDetails(List<String> nodeIds) {
//        executor.execute(() -> {
//            Map<Object, Object> postData = new HashMap<>();
//            postData.put("ids", nodeIds);
//            Instant begin = Instant.now();
//            JsonNode nodeDetails = HttpUtils.postJSON("https://wwwdev.ebi.ac.uk/intact/ws/graph/network/node/details", postData, manager);
//            System.out.println("Query answered in " + Duration.between(begin, Instant.now()).toMillis());
//            if (nodeDetails != null) {
//                for (JsonNode nodeDetail : nodeDetails) {
//                    String nodeId = nodeDetail.get("id").textValue();
//                    NodePanel nodePanel = nodeIdToNodePanel.get(nodeId);
//
//                    JsonNode aliases = nodeDetail.get("aliases");
//                    List<Identifier> xrefs = new ArrayList<>();
//                    for (JsonNode xref : nodeDetail.get("xrefs")) {
//                        JsonNode database = xref.get("database");
//
//                        String databaseName = database.get("shortName").textValue();
//                        OntologyIdentifier databaseIdentifier = new OntologyIdentifier(database.get("identifier").textValue());
//
//                        String identifier = xref.get("identifier").textValue();
//                        String qualifier = xref.get("qualifier").textValue();
//
//                        xrefs.add(new Identifier(nodePanel.node, databaseName, databaseIdentifier, identifier, qualifier));
//                    }
//                }
//            }
//            System.out.println("Query result parsed in " + Duration.between(begin, Instant.now()).toMillis());
//        });
//    }
}



