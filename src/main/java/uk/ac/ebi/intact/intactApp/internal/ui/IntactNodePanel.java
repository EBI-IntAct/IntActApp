package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ViewUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays information about a protein taken from STRING
 *
 * @author Scooter Morris
 */
public class IntactNodePanel extends AbstractIntactPanel {

    private JCheckBox stringLabels;
    private JPanel tissuesPanel = null;
    private JPanel compartmentsPanel = null;
    private JPanel nodesPanel = null;
    private JButton highlightQuery;
    private boolean updating = false;
    private Color defaultBackground;
    // private List<CyNode> highlightNodes = null;
    // private JCheckBox highlightCheck = null;

    public IntactNodePanel(final IntactManager manager) {
        super(manager);
        filters.get(currentNetwork).put("tissue", new HashMap<>());
        filters.get(currentNetwork).put("compartment", new HashMap<>());
        init();
        revalidate();
        repaint();
    }

    public void updateControls() {
        updating = true;
        stringLabels.setSelected(manager.showEnhancedLabels());
        updating = false;
    }

    private void init() {
        setLayout(new GridBagLayout());

        EasyGBC c = new EasyGBC();

        JPanel mainPanel = new JPanel();
        {
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.setBackground(defaultBackground);
            EasyGBC d = new EasyGBC();

            mainPanel.add(createCompartmentsPanel(), d.down().anchor("west").expandHoriz());
            mainPanel.add(createNodesPanel(), d.down().anchor("west").expandHoriz());
            mainPanel.add(new JLabel(""), d.down().anchor("west").expandBoth());
        }
        JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        add(scrollPane, c.down().anchor("west").expandBoth());
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        EasyGBC d = new EasyGBC();
        controlPanel.setLayout(new GridBagLayout());

        EasyGBC upperGBC = new EasyGBC();
        JPanel upperPanel = new JPanel(new GridBagLayout());

        {
            stringLabels = new JCheckBox("STRING style labels");
            stringLabels.setFont(labelFont);
            stringLabels.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (updating) return;
                    manager.execute(
                            manager.getEnhancedLabelsTaskFactory().createTaskIterator(manager.getCurrentNetworkView()), true);
                }
            });
            upperPanel.add(stringLabels, upperGBC.left().down().noInsets().noExpand());
        }
//
//        upperPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
//
//        controlPanel.add(upperPanel, d.anchor("northwest").expandHoriz());
//
//        // controlPanel.add(new JLabel());
//
//        JPanel lowerPanel = new JPanel();
//        GridLayout layout2 = new GridLayout(2, 2);
//        layout2.setVgap(0);
//        lowerPanel.setLayout(layout2);
//        {
//            JButton getEnrichment = new JButton("Functional enrichment");
//            getEnrichment.setFont(labelFont);
//            lowerPanel.add(getEnrichment);
//            getEnrichment.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    GetEnrichmentTaskFactory tf = new GetEnrichmentTaskFactory(manager, true);
//                    ShowEnrichmentPanelTaskFactory showTf = manager.getShowEnrichmentPanelTaskFactory();
//                    tf.setShowEnrichmentPanelFactory(showTf);
//                    manager.execute(tf.createTaskIterator(currentNetwork), false);
//                }
//            });
//        }
//
//        {
//            JButton getPublications = new JButton("Enriched publications");
//            getPublications.setFont(labelFont);
//            lowerPanel.add(getPublications);
//            getPublications.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    GetPublicationsTaskFactory tf = new GetPublicationsTaskFactory(manager, true);
//                    ShowPublicationsPanelTaskFactory showTf = manager.getShowPublicationsPanelTaskFactory();
//                    tf.setShowPublicationsPanelFactory(showTf);
//                    manager.execute(tf.createTaskIterator(currentNetwork), false);
//                }
//            });
//        }
//
//        {
//            highlightQuery = new JButton("Select query");
//            highlightQuery.setFont(labelFont);
//
//            // See if we have anything in "query term"
//            if (!ModelUtils.haveQueryTerms(currentNetwork))
//                highlightQuery.setEnabled(false);
//
//            highlightQuery.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    ModelUtils.selectQueryTerms(currentNetwork);
//                }
//            });
//            lowerPanel.add(highlightQuery);
//        }
//        controlPanel.add(lowerPanel, d.down().anchor("west").expandHoriz());

        updateControls();
        controlPanel.setMaximumSize(new Dimension(300, 100));
        controlPanel.setAlignmentX(LEFT_ALIGNMENT);
        return controlPanel;
    }

    private JPanel createCompartmentsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        EasyGBC c = new EasyGBC();
        List<String> compartmentList = ModelUtils.getCompartmentList(currentNetwork);
        for (String compartment : compartmentList) {
            panel.add(createFilterSlider("compartment", compartment, currentNetwork, true, 500.0),
                    c.anchor("west").down().expandHoriz());
        }
        CollapsablePanel collapsablePanel = new CollapsablePanel(iconFont, "Compartment filters", panel, true, 10);
//        collapsablePanel.setBorder(BorderFactory.createEtchedBorder());
        return collapsablePanel;
    }

    private void updateCompartmentsPanel() {
        if (compartmentsPanel == null) return;
        compartmentsPanel.removeAll();
        EasyGBC c = new EasyGBC();
        List<String> compartmentsList = ModelUtils.getCompartmentList(currentNetwork);
        for (String compartments : compartmentsList) {
            compartmentsPanel.add(createFilterSlider("compartment", compartments, currentNetwork, true, 500.0),
                    c.anchor("west").down().expandHoriz());
        }
        return;
    }

    private JPanel createNodesPanel() {
        nodesPanel = new JPanel();
        nodesPanel.setLayout(new GridBagLayout());
        EasyGBC c = new EasyGBC();

        if (currentNetwork != null) {
            List<CyNode> nodes = CyTableUtil.getNodesInState(currentNetwork, CyNetwork.SELECTED, true);
            for (CyNode node : nodes) {
                JPanel newPanel = createNodePanel(node);
                newPanel.setAlignmentX(LEFT_ALIGNMENT);

                nodesPanel.add(newPanel, c.anchor("west").down().expandHoriz());
            }
        }
        nodesPanel.setAlignmentX(LEFT_ALIGNMENT);
        CollapsablePanel collapsablePanel = new CollapsablePanel(iconFont, "Selected nodes", nodesPanel, false, 10);
//        collapsablePanel.setBorder(BorderFactory.createEtchedBorder());
        return collapsablePanel;
    }

    // Hide all nodes who's values are less than "value"
    void doFilter(String type) {
        Map<String, Double> filter = filters.get(currentNetwork).get(type);
        CyNetworkView view = manager.getCurrentNetworkView();
        CyNetwork net = view.getModel();
        for (CyNode node : currentNetwork.getNodeList()) {
            CyRow nodeRow = currentNetwork.getRow(node);
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
        IntactNode sNode = new IntactNode(manager.getIntactNetwork(currentNetwork), node);
        EasyGBC c = new EasyGBC();
        panel.setLayout(new GridBagLayout());

        int leftBorder = 30;

        {
            JLabel lbl = new JLabel("Crosslinks");
            lbl.setFont(labelFont);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            lbl.setBorder(BorderFactory.createEmptyBorder(5, leftBorder, 5, 0));
            panel.add(lbl, c.anchor("west").down().noExpand());

            JPanel crosslinkPanel = new JPanel();
            GridLayout layout = new GridLayout(2, 4);
            crosslinkPanel.setLayout(layout);
            crosslinkPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            if (sNode.haveUniprot()) {
                JLabel link = new SwingLink("UniProt", sNode.getUniprotURL(), openBrowser);
                link.setFont(textFont);
                crosslinkPanel.add(link);
            }

            crosslinkPanel.setAlignmentX(LEFT_ALIGNMENT);
            crosslinkPanel.setBackground(new Color(255,255,255,0));
            panel.add(crosslinkPanel, c.anchor("west").down().noExpand());
        }

        {
            JLabel lbl = new JLabel("Description");
            lbl.setFont(labelFont);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            lbl.setBorder(BorderFactory.createEmptyBorder(10, leftBorder, 5, 0));
            panel.add(lbl, c.anchor("west").down().expandHoriz());

            JLabel description = new JLabel("<html><body style='width:250px;font-size:8px'>" + sNode.getDescription() + "</body></html>");
            description.setBorder(BorderFactory.createEmptyBorder(0, leftBorder + 5, 0, 0));
            description.setAlignmentX(LEFT_ALIGNMENT);
            description.setBackground(new Color(255,255,255,0));
            panel.add(description, c.anchor("west").down().expandBoth());

        }

        CollapsablePanel collapsablePanel = new CollapsablePanel(iconFont, sNode.getDisplayName(), panel, false, 10);
//        Border etchedBorder = BorderFactory.createEtchedBorder();
        Border emptyBorder = BorderFactory.createEmptyBorder(0, 15, 0, 0);
//        collapsablePanel.setBorder(BorderFactory.createCompoundBorder(emptyBorder, etchedBorder));
        collapsablePanel.setBorder(emptyBorder);
        collapsablePanel.setBackground(new Color(255,255,255,0));
        return collapsablePanel;
    }

    public void networkChanged(CyNetwork newNetwork) {
        this.currentNetwork = newNetwork;
        if (currentNetwork == null) {
            // Hide results panel?
            if (tissuesPanel != null)
                tissuesPanel.removeAll();
            if (compartmentsPanel != null)
                compartmentsPanel.removeAll();
            return;
        }

        if (!filters.containsKey(currentNetwork)) {
            filters.put(currentNetwork, new HashMap<>());
            filters.get(currentNetwork).put("tissue", new HashMap<>());
            filters.get(currentNetwork).put("compartment", new HashMap<>());
        }

        // We need to get the view for the new network since we haven't actually switched yet
        CyNetworkView networkView = ModelUtils.getNetworkView(manager, currentNetwork);
        if (networkView != null) {
            if (manager.highlightNeighbors()) {
                doHighlight(networkView);
            } else {
                clearHighlight(networkView);
            }
        }
        updateCompartmentsPanel();
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
