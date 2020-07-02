package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels;

import com.google.common.collect.Comparators;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.NodeBasics;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.NodeDetails;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.NodeFeatures;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.identifiers.NodeIdentifiers;
import uk.ac.ebi.intact.app.internal.ui.panels.filters.FilterPanel;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.NodeSchematic;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class NodeDetailPanel extends AbstractDetailPanel {
    private JPanel nodesPanel = null;
    private CollapsablePanel selectedNodes;
    private static final int MAXIMUM_SELECTED_NODE_SHOWN = 100;
    private final EasyGBC layoutHelper = new EasyGBC();
    public volatile boolean selectionRunning;
    private final Map<String, NodePanel> nodeIdToNodePanel = new Hashtable<>();
    private final JPanel filtersPanel = new JPanel(new GridBagLayout());
    private final EasyGBC filterHelper = new EasyGBC();
    private final Map<Class<? extends Filter>, FilterPanel> filterPanels = new HashMap<>();

    public NodeDetailPanel(final Manager manager) {
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
            mainPanel.setBackground(UIColors.lightBackground);
            EasyGBC d = new EasyGBC();
            CollapsablePanel filters = new CollapsablePanel("Filters", filtersPanel, true);
            filters.setBackground(UIColors.lightBackground);
            mainPanel.add(filters, d.down().anchor("north").expandHoriz());
            mainPanel.add(createNodesPanel(), d.down().anchor("north").expandHoriz());
            mainPanel.add(Box.createVerticalGlue(), d.down().expandVert());
        }
        JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        scrollPane.getVerticalScrollBar().setBlockIncrement(10);
        add(scrollPane, c.down().anchor("west").expandBoth());
    }

    public void setupFilters(List<Filter<? extends Node>> nodeFilters) {
        for (Filter<? extends Node> filter : nodeFilters) {
            if (!filterPanels.containsKey(filter.getClass())) {
                FilterPanel<?> filterPanel = FilterPanel.createFilterPanel(filter);
                if (filterPanel != null) {
                    filtersPanel.add(filterPanel, filterHelper.down().expandHoriz());
                    filterPanels.put(filter.getClass(), filterPanel);
                }
            } else {
                filterPanels.get(filter.getClass()).setFilter(filter);
            }
        }
        hideDisabledFilters();
    }

    public void hideDisabledFilters() {
        for (FilterPanel<?> filterPanel : filterPanels.values()) {
            filterPanel.setVisible(filterPanel.getFilter().isEnabled());
        }
    }

    private JPanel createNodesPanel() {
        nodesPanel = new JPanel();
        nodesPanel.setBackground(UIColors.lightBackground);
        nodesPanel.setLayout(new GridBagLayout());

        selectedNodes(CyTableUtil.getNodesInState(currentNetwork.getCyNetwork(), CyNetwork.SELECTED, true));

        nodesPanel.setAlignmentX(LEFT_ALIGNMENT);
        selectedNodes = new CollapsablePanel("Selected nodes info", nodesPanel, false);
        return selectedNodes;
    }


    public void networkChanged(Network newNetwork) {
        this.currentNetwork = newNetwork;
        selectedNodes(newNetwork.getSelectedNodes());
    }


    public void selectedNodes(Collection<CyNode> nodes) {
        if (checkCurrentNetwork() && checkCurrentView()) {
            selectionRunning = true;

            List<Node> iNodes = nodes.stream()
                    .map(node -> new Node(currentNetwork, node))
                    .filter(node -> currentView.visibleNodes.contains(node))
                    .collect(Comparators.least(MAXIMUM_SELECTED_NODE_SHOWN, Node::compareTo));

            List<String> nodesIds = iNodes.stream().map(iNode -> iNode.preferredId).collect(Collectors.toList());

            for (Node node : iNodes) {
                if (!selectionRunning) {
                    break;
                }

                if (!nodeIdToNodePanel.containsKey(node.preferredId)) {
                    NodePanel newPanel = new NodePanel(node);
                    newPanel.setAlignmentX(LEFT_ALIGNMENT);
                    nodesPanel.add(newPanel, layoutHelper.anchor("west").down().expandHoriz());
                    nodeIdToNodePanel.put(node.preferredId, newPanel);
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
        final Node node;

        public NodePanel(Node node) {
            super("", !(selectedNodes == null || selectedNodes.collapseAllButton.isExpanded()));
            this.node = node;
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setAlignmentX(LEFT_ALIGNMENT);
            setBackground(UIColors.lightBackground);
            List<Feature> features = node.getFeatures();
            setHeader(new NodeSchematic(node, features, openBrowser));
            content.add(new NodeBasics(node, openBrowser));
            nodeFeatures = new NodeFeatures(node, features, openBrowser, true, null);
            content.add(nodeFeatures);
            content.add(new NodeIdentifiers(node, openBrowser));
            nodeDetails = new NodeDetails(node, openBrowser);
            content.add(nodeDetails);
        }

        public void delete() {
            nodeFeatures.deleteEdgeSelectionCheckboxes();
        }
    }
}



