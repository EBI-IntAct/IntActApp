package uk.ac.ebi.intact.app.internal.ui.panels.detail;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.*;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.events.NetworkCreatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.NetworkCreatedListener;
import uk.ac.ebi.intact.app.internal.model.events.ViewUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.ViewUpdatedListener;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.SummaryViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.EvidenceViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.MutationViewTaskFactory;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.EdgeDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.LegendDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.NodeDetailPanel;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;
import uk.ac.ebi.intact.app.internal.utils.TimeUtils;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;


public class DetailPanel extends JPanel
        implements CytoPanelComponent2,
        SetCurrentNetworkListener,
        SetCurrentNetworkViewListener,
        SelectedNodesAndEdgesListener,
        NetworkCreatedListener,
        ViewUpdatedListener {

    private static final Icon icon = IconUtils.createImageIcon("/IntAct/DIGITAL/Gradient_over_Transparent/favicon_32x32.ico");
    final Manager manager;

    private final JRadioButton summaryViewType = new JRadioButton("Summary");
    private final JRadioButton evidenceViewType = new JRadioButton("Evidence");
    private final JRadioButton mutationViewType = new JRadioButton("Mutation");

    private final SummaryViewTaskFactory summaryViewTaskFactory;
    private final EvidenceViewTaskFactory evidenceViewTaskFactory;
    private final MutationViewTaskFactory mutationViewTaskFactory;

    private final NodeDetailPanel nodePanel;
    private final EdgeDetailPanel edgePanel;
    private final LegendDetailPanel legendPanel;
    private boolean registered;
    private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);


    public DetailPanel(final Manager manager) {
        this.manager = manager;
//        manager.data.addIntactNetworkCreatedListener(this);
//        manager.data.addIntactViewChangedListener(this);
        manager.utils.registerAllServices(this, new Properties());
        this.setLayout(new BorderLayout());

        summaryViewTaskFactory = new SummaryViewTaskFactory(manager, true);
        evidenceViewTaskFactory = new EvidenceViewTaskFactory(manager, true);
        mutationViewTaskFactory = new MutationViewTaskFactory(manager, true);

        ButtonGroup viewTypes = new ButtonGroup();
        viewTypes.add(summaryViewType);
        viewTypes.add(evidenceViewType);
        viewTypes.add(mutationViewType);


        NetworkView view = manager.data.getCurrentIntactNetworkView();
        summaryViewType.addActionListener(e -> manager.utils.execute(summaryViewTaskFactory.createTaskIterator()));
        evidenceViewType.addActionListener(e -> manager.utils.execute(evidenceViewTaskFactory.createTaskIterator()));
        mutationViewType.addActionListener(e -> manager.utils.execute(mutationViewTaskFactory.createTaskIterator()));
        if (view != null) {
            switch (view.getType()) {
                case SUMMARY:
                    summaryViewType.setSelected(true);
                    break;
                case EVIDENCE:
                    evidenceViewType.setSelected(true);
                    break;
                case MUTATION:
                    mutationViewType.setSelected(true);
                    break;
            }
        }

        JPanel viewTypesPanel = new JPanel(new GridLayout(3, 1));
        viewTypesPanel.setBorder(BorderFactory.createTitledBorder("View types"));
        viewTypesPanel.add(summaryViewType);
        viewTypesPanel.add(evidenceViewType);
        viewTypesPanel.add(mutationViewType);
        JPanel upperPanel = new JPanel(new GridLayout(1, 2));
        upperPanel.add(viewTypesPanel);
        this.add(upperPanel, BorderLayout.NORTH);


        legendPanel = new LegendDetailPanel(manager);
        tabs.add("Legend", legendPanel);
        nodePanel = new NodeDetailPanel(manager);
        tabs.add("Nodes", nodePanel);
        edgePanel = new EdgeDetailPanel(manager);
        tabs.add("Edges", edgePanel);

        this.add(tabs, BorderLayout.CENTER);
//        this.add(new VersionPanel(), BorderLayout.SOUTH);
        manager.utils.setDetailPanel(this);
        registered = true;
        if (view != null) {
            setupFilters(view);
            legendPanel.viewUpdated(view.getType());
        }
        revalidate();
        repaint();
    }


    public void showCytoPanel() {
        CySwingApplication swingApplication = manager.utils.getService(CySwingApplication.class);
        CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);
        if (!registered) {
            manager.utils.registerService(this, CytoPanelComponent.class, new Properties());
            registered = true;
        }
        if (cytoPanel.getState() == CytoPanelState.HIDE)
            cytoPanel.setState(CytoPanelState.DOCK);

        // Tell tabs
        Network currentNetwork = manager.data.getCurrentNetwork();
        if (currentNetwork != null) {
            nodePanel.networkChanged(currentNetwork);
            edgePanel.networkChanged(currentNetwork);
            legendPanel.networkChanged(currentNetwork);
        }
    }

    public void hideCytoPanel() {
        manager.utils.unregisterService(this, CytoPanelComponent.class);
        registered = false;
    }

    public String getIdentifier() {
        return "uk.ac.ebi.intact.app.details";
    }

    public Component getComponent() {
        // TODO Auto-generated method stub
        return this;
    }

    public CytoPanelName getCytoPanelName() {
        // TODO Auto-generated method stub
        return CytoPanelName.EAST;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getTitle() {
        return "IntAct";
    }


    private Instant lastSelection = Instant.now();

    @Override
    public void handleEvent(SelectedNodesAndEdgesEvent event) {
        if (!registered) return;
        if (Instant.now().minusMillis(200).isAfter(lastSelection)) {

            if (nodePanel.selectionRunning || edgePanel.selectionRunning) {
                nodePanel.selectionRunning = false;
                edgePanel.selectionRunning = false;
                TimeUtils.sleep(200);
            }
            lastSelection = Instant.now();
            Collection<CyNode> selectedNodes = event.getSelectedNodes();
            Collection<CyEdge> selectedEdges = event.getSelectedEdges();
            boolean nodesSelected = !selectedNodes.isEmpty();
            boolean edgesSelected = !selectedEdges.isEmpty();
            if (nodesSelected && edgesSelected && tabs.getSelectedComponent() == legendPanel) {
                tabs.setSelectedComponent(nodePanel);
            } else if (nodesSelected && selectedEdges.isEmpty()) {
                tabs.setSelectedComponent(nodePanel);
            } else if (selectedNodes.isEmpty() && edgesSelected) {
                tabs.setSelectedComponent(edgePanel);
            }

            new Thread(() -> nodePanel.selectedNodes(selectedNodes)).start();
            new Thread(() -> edgePanel.selectedEdges(selectedEdges)).start();
        }
    }

    private void updateRadioButtons(CyNetworkView cyView) {
        NetworkView networkView = manager.data.getNetworkView(cyView);
        if (networkView != null) {
            switch (networkView.getType()) {
                case SUMMARY:
                    summaryViewType.setSelected(true);
                    break;
                case EVIDENCE:
                    evidenceViewType.setSelected(true);
                    break;
                case MUTATION:
                    mutationViewType.setSelected(true);
                    break;
            }
        }
    }

    @Override
    public void handleEvent(SetCurrentNetworkEvent event) {
        Network network = manager.data.getNetwork(event.getNetwork());
        if (network != null && ModelUtils.ifHaveIntactNS(network.getCyNetwork())) {
            if (!registered) {
                showCytoPanel();
            }
            // Tell tabs
            nodePanel.networkChanged(network);
            edgePanel.networkChanged(network);
            legendPanel.networkChanged(network);
        } else {
            hideCytoPanel();
        }
    }

    public void setupFilters(NetworkView view) {
        List<Filter<? extends Edge>> edgeFilters = new ArrayList<>();
        List<Filter<? extends Node>> nodeFilters = new ArrayList<>();
        for (Filter<?> filter : view.getFilters()) {
            if (Edge.class.isAssignableFrom(filter.elementType)) {
                edgeFilters.add((Filter<? extends Edge>) filter);
            } else if (Node.class.isAssignableFrom(filter.elementType)) {
                nodeFilters.add((Filter<? extends Node>) filter);
            }
        }
        edgePanel.setupFilters(edgeFilters);
        nodePanel.setupFilters(nodeFilters);

    }


    @Override
    public void handleEvent(SetCurrentNetworkViewEvent e) {
        CyNetworkView cyView = e.getNetworkView();
        if (cyView != null) {
            updateRadioButtons(cyView);
            legendPanel.networkViewChanged(cyView);
            edgePanel.networkViewChanged(cyView);
            NetworkView view = manager.data.getNetworkView(cyView);
            if (view != null) {
                setupFilters(view);
            }
        }
    }

    @Override
    public void handleEvent(NetworkCreatedEvent event) {
        if (!registered) {
            showCytoPanel();
        }
        if (nodePanel != null) {
            Network newNetwork = event.getNewNetwork();
            nodePanel.networkChanged(newNetwork);
            edgePanel.networkChanged(newNetwork);
            legendPanel.networkChanged(newNetwork);
        }
    }

    @Override
    public void handleEvent(ViewUpdatedEvent event) {
        legendPanel.viewUpdated(event.newType);
        nodePanel.viewUpdated();
        edgePanel.viewUpdated();
        switch (event.newType) {
            case SUMMARY:
                summaryViewType.setSelected(true);
                break;
            case EVIDENCE:
                evidenceViewType.setSelected(true);
                break;
            case MUTATION:
                mutationViewType.setSelected(true);
                break;
        }
    }
}
