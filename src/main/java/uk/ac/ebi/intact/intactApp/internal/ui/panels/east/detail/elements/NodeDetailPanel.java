package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements;

import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.ui.components.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeBasics;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeSchematic;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeSummary;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeXRefs.NodeIdentifiers;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeXRefs.NodeXRefs;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.intactApp.internal.utils.ViewUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Displays information about a protein taken from STRING
 *
 * @author Scooter Morris
 */
public class NodeDetailPanel extends AbstractDetailPanel {

    private JPanel nodesPanel = null;
    private final int MAXIMUM_SELECTED_NODE_SHOWN = 100;


    private Color defaultBackground;

    public volatile boolean selectionRunning;
    private final Map<CyNode, NodePanel> nodeToPanel = new HashMap<>();
    private final EasyGBC layoutHelper;
    // private List<CyNode> highlightNodes = null;
    // private JCheckBox highlightCheck = null;

    public NodeDetailPanel(final IntactManager manager) {
        super(manager);
        init();
        revalidate();
        repaint();
        layoutHelper = new EasyGBC();
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
        EasyGBC c = new EasyGBC();

        if (currentINetwork != null) {
            List<CyNode> nodes = CyTableUtil.getNodesInState(currentINetwork.getNetwork(), CyNetwork.SELECTED, true);
            int counter = 0;
            for (CyNode node : nodes) {
                if (counter++ > MAXIMUM_SELECTED_NODE_SHOWN)
                    break;
                NodePanel newPanel = new NodePanel(node);
                newPanel.setAlignmentX(LEFT_ALIGNMENT);

                nodesPanel.add(newPanel, c.anchor("west").down().expandHoriz());
                nodeToPanel.put(node, newPanel);
            }
        }
        nodesPanel.setAlignmentX(LEFT_ALIGNMENT);
        return new CollapsablePanel("Selected nodes", nodesPanel, false, 10);
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

    private class NodePanel extends CollapsablePanel {

        private final NodeSummary nodeSummary;

        public NodePanel(CyNode node) {
            super("", false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setAlignmentX(LEFT_ALIGNMENT);
            setBackground(backgroundColor);
            IntactNode iNode = new IntactNode(currentINetwork, node);
            setLabel(iNode.name);
            content.add(new NodeSchematic(iNode, iNode.getFeatures(), openBrowser));
            content.add(new NodeBasics(iNode, openBrowser));
            nodeSummary = new NodeSummary(iNode, iNode.getFeatures(), openBrowser, true, true, true);
            content.add(nodeSummary);
            content.add(new NodeIdentifiers(iNode, openBrowser));
            content.add(new NodeXRefs(iNode, openBrowser));
        }

        public void delete() {
            nodeSummary.deleteEdgeSelectionCheckboxes();
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
            int counterNode = 0;
            for (CyNode node : nodes) {
                if (!selectionRunning || counterNode++ > MAXIMUM_SELECTED_NODE_SHOWN) {
                    break;
                }
                if (!nodeToPanel.containsKey(node)) {
                    NodePanel newPanel = new NodePanel(node);
                    newPanel.setAlignmentX(LEFT_ALIGNMENT);
                    nodesPanel.add(newPanel, layoutHelper.anchor("west").down().expandHoriz());
                    nodeToPanel.put(node, newPanel);

                }
            }
            HashSet<CyNode> unselectedNodes = new HashSet<>(nodeToPanel.keySet());
            unselectedNodes.removeAll(nodes);
            for (CyNode unselectedNode : unselectedNodes) {
                NodePanel nodePanel = nodeToPanel.get(unselectedNode);
                nodePanel.delete();
                nodesPanel.remove(nodePanel);
                nodeToPanel.remove(unselectedNode);
            }

            selectionRunning = false;
        }

        revalidate();
        repaint();
    }

    private void doHighlight(CyNetworkView networkView) {

        if (networkView != null) {
            List<CyNode> nodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, Boolean.TRUE);
            if (nodes == null || nodes.size() == 0) {
                return;
            }

            ViewUtils.clearHighlight(manager, networkView);
            ViewUtils.highlight(manager, networkView, nodes);
        }
    }

    private void clearHighlight(CyNetworkView networkView) {
        ViewUtils.clearHighlight(manager, networkView);
    }
}
