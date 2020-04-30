package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements;

import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.DbIdentifiersToLink;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.ui.components.SwingLink;
import uk.ac.ebi.intact.intactApp.internal.ui.components.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.AbstractDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
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

    private JCheckBox stringLabels;
    private JPanel tissuesPanel = null;
    private JPanel compartmentsPanel = null;
    private JPanel nodesPanel = null;
    private JButton highlightQuery;

    private Color defaultBackground;
    // private List<CyNode> highlightNodes = null;
    // private JCheckBox highlightCheck = null;

    public NodeDetailPanel(final IntactManager manager) {
        super(manager);
        init();
        revalidate();
        repaint();
    }

    public void updateControls() {
        stringLabels.setSelected(manager.showEnhancedLabels());
    }

    private void init() {
        setLayout(new GridBagLayout());

        EasyGBC c = new EasyGBC();

        JPanel mainPanel = new JPanel();
        {
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.setBackground(backgroundColor);
            EasyGBC d = new EasyGBC();

            mainPanel.add(createNodesPanel(), d.down().anchor("west").expandHoriz());
            mainPanel.add(new JLabel(""), d.down().anchor("west").expandBoth());
        }
        JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        add(scrollPane, c.down().anchor("west").expandBoth());
    }

    private JPanel createNodesPanel() {
        nodesPanel = new JPanel();
        nodesPanel.setBackground(backgroundColor);
        nodesPanel.setLayout(new GridBagLayout());
        EasyGBC c = new EasyGBC();

        if (currentINetwork != null) {
            List<CyNode> nodes = CyTableUtil.getNodesInState(currentINetwork.getNetwork(), CyNetwork.SELECTED, true);
            for (CyNode node : nodes) {
                JPanel newPanel = createNodePanel(node);
                newPanel.setAlignmentX(LEFT_ALIGNMENT);

                nodesPanel.add(newPanel, c.anchor("west").down().expandHoriz());
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


    private JPanel createNodePanel(CyNode node) {
        JPanel panel = new JPanel();
        IntactNode sNode = new IntactNode(currentINetwork, node);
        EasyGBC c = new EasyGBC();
        panel.setLayout(new GridBagLayout());

        {
            JLabel lbl = new JLabel("Identifiers");
            lbl.setFont(labelFont);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            panel.add(lbl, c.anchor("west").down().noExpand());

            JPanel crosslinkPanel = new JPanel(new GridBagLayout());
            EasyGBC d = new EasyGBC();
            crosslinkPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            Map<String, List<String>> dbToRefs = new HashMap<>();

            for (CyRow crossRefs : currentINetwork.getXRefsTable().getMatchingRows(ModelUtils.NODE_REF, node.getSUID())) {
                String dbName = crossRefs.get(ModelUtils.XREF_DB_NAME, String.class);
                String id = crossRefs.get(ModelUtils.XREF_ID, String.class);
                if (!dbToRefs.containsKey(dbName)) {
                    List<String> refs = new ArrayList<>();
                    refs.add(id);
                    dbToRefs.put(dbName, refs);
                } else {
                    dbToRefs.get(dbName).add(id);
                }
            }

            for (Map.Entry<String, List<String>> entry : dbToRefs.entrySet()) {
                crosslinkPanel.add(new JLabel("• " + DbIdentifiersToLink.fancy(entry.getKey()) + " : "), d.down().anchor("west").noExpand());
                d.up();
                for (String ref : entry.getValue()) {
                    JLabel link = new SwingLink(ref, DbIdentifiersToLink.getLink(entry.getKey(), ref), openBrowser);
                    link.setFont(textFont);
                    crosslinkPanel.add(link, d.down().right().anchor("west").expandHoriz());
                }
                crosslinkPanel.add(Box.createVerticalStrut(4), d.down());
            }
            crosslinkPanel.setBackground(backgroundColor);
            panel.add(crosslinkPanel, c.anchor("west").down().noExpand());
        }

        {
            JLabel lbl = new JLabel("Description");
            lbl.setFont(labelFont);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
            panel.add(lbl, c.anchor("west").down().expandHoriz());

            JLabel description = new JLabel("<html><body style='width:250px;font-size:8px'>" + sNode.getDescription() + "</body></html>");
            description.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            description.setAlignmentX(LEFT_ALIGNMENT);
            description.setBackground(new Color(255, 255, 255, 0));
            panel.add(description, c.anchor("west").down().expandBoth());

        }
        panel.setBackground(backgroundColor);
        return new CollapsablePanel(sNode.getDisplayName(), panel, false, 10);
    }

    public void networkChanged(IntactNetwork newNetwork) {
        this.currentINetwork = newNetwork;
        if (newNetwork == null) {
            // Hide results panel?
            if (tissuesPanel != null)
                tissuesPanel.removeAll();
            if (compartmentsPanel != null)
                compartmentsPanel.removeAll();
            return;
        }

        CyNetwork network = newNetwork.getNetwork();
        if (!filters.containsKey(network)) {
            filters.put(network, new HashMap<>());
            filters.get(network).put("tissue", new HashMap<>());
            filters.get(network).put("compartment", new HashMap<>());
        }

        // We need to get the view for the new network since we haven't actually switched yet
        CyNetworkView networkView = ModelUtils.getNetworkView(manager, network);
        if (networkView != null) {
            if (manager.highlightNeighbors()) {
                doHighlight(networkView);
            } else {
                clearHighlight(networkView);
            }
        }
    }

    public void selectedNodes(Collection<CyNode> nodes) {
        // Clear the nodes panel
        nodesPanel.removeAll();
        EasyGBC c = new EasyGBC();
        ViewUtils.clearHighlight(manager, manager.getCurrentNetworkView());

        for (CyNode node : nodes) {
            JPanel newPanel = createNodePanel(node);
            newPanel.setAlignmentX(LEFT_ALIGNMENT);

            nodesPanel.add(newPanel, c.anchor("west").down().expandHoriz());
        }

        if (manager.highlightNeighbors()) {
            doHighlight(manager.getCurrentNetworkView());
        } else {
            clearHighlight(manager.getCurrentNetworkView());
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
