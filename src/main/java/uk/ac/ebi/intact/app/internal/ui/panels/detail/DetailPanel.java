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
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.events.IntactNetworkCreatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.IntactNetworkCreatedListener;
import uk.ac.ebi.intact.app.internal.model.events.IntactViewChangedEvent;
import uk.ac.ebi.intact.app.internal.model.events.IntactViewTypeChangedListener;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.CollapseViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.ExpandViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.MutationViewTaskFactory;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.EdgeDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.LegendDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.NodeDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.VersionPanel;
import uk.ac.ebi.intact.app.internal.utils.tables.ModelUtils;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;
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
        IntactNetworkCreatedListener,
        IntactViewTypeChangedListener {

    private static final Icon icon = IconUtils.createImageIcon("/IntAct/DIGITAL/Gradient_over_Transparent/favicon_32x32.ico");
    final Manager manager;

    private final JRadioButton collapsedViewType = new JRadioButton("Collapse");
    private final JRadioButton expandedViewType = new JRadioButton("Expanded");
    private final JRadioButton mutationViewType = new JRadioButton("Mutation");

    private final CollapseViewTaskFactory collapseViewTaskFactory;
    private final ExpandViewTaskFactory expandViewTaskFactory;
    private final MutationViewTaskFactory mutationViewTaskFactory;

    private final NodeDetailPanel nodePanel;
    private final EdgeDetailPanel edgePanel;
    private final LegendDetailPanel legendPanel;
    private boolean registered;
    private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);


    public DetailPanel(final Manager manager) {
        this.manager = manager;
        manager.data.addIntactNetworkCreatedListener(this);
        manager.data.addIntactViewChangedListener(this);
        this.setLayout(new BorderLayout());

        collapseViewTaskFactory = new CollapseViewTaskFactory(manager, true);
        expandViewTaskFactory = new ExpandViewTaskFactory(manager, true);
        mutationViewTaskFactory = new MutationViewTaskFactory(manager, true);

        ButtonGroup viewTypes = new ButtonGroup();
        viewTypes.add(collapsedViewType);
        viewTypes.add(expandedViewType);
        viewTypes.add(mutationViewType);


        NetworkView view = manager.data.getCurrentIntactNetworkView();
        collapsedViewType.addActionListener(e -> manager.utils.execute(collapseViewTaskFactory.createTaskIterator()));
        expandedViewType.addActionListener(e -> manager.utils.execute(expandViewTaskFactory.createTaskIterator()));
        mutationViewType.addActionListener(e -> manager.utils.execute(mutationViewTaskFactory.createTaskIterator()));
        if (view != null) {
            switch (view.getType()) {
                case COLLAPSED:
                    collapsedViewType.setSelected(true);
                    break;
                case EXPANDED:
                    expandedViewType.setSelected(true);
                    break;
                case MUTATION:
                    mutationViewType.setSelected(true);
                    break;
            }
        }

        JPanel viewTypesPanel = new JPanel(new GridLayout(3, 1));
        viewTypesPanel.setBorder(BorderFactory.createTitledBorder("View types"));
        viewTypesPanel.add(collapsedViewType);
        viewTypesPanel.add(expandedViewType);
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
        this.add(new VersionPanel(), BorderLayout.SOUTH);
        manager.utils.setDetailPanel(this);
        manager.utils.registerService(this, SetCurrentNetworkListener.class, new Properties());
        manager.utils.registerService(this, SetCurrentNetworkViewListener.class, new Properties());
        manager.utils.registerService(this, SelectedNodesAndEdgesListener.class, new Properties());
        registered = true;
        if (view != null) {
            setupFilters(view);
            legendPanel.viewTypeChanged(view.getType());
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
        if (Instant.now().minusMillis(500).isAfter(lastSelection)) {

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
                case COLLAPSED:
                    collapsedViewType.setSelected(true);
                    break;
                case EXPANDED:
                    expandedViewType.setSelected(true);
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
    public void handleEvent(IntactNetworkCreatedEvent event) {
        if (!registered) {
            showCytoPanel();
        }
        if (nodePanel != null) {
            // Tell tabs
            Network newINetwork = event.getNewINetwork();
            nodePanel.networkChanged(newINetwork);
            edgePanel.networkChanged(newINetwork);
            legendPanel.networkChanged(newINetwork);
        }
    }

    @Override
    public void handleEvent(IntactViewChangedEvent event) {
        legendPanel.viewTypeChanged(event.newType);
        edgePanel.viewTypeChanged();
        switch (event.newType) {
            case COLLAPSED:
                collapsedViewType.setSelected(true);
                break;
            case EXPANDED:
                expandedViewType.setSelected(true);
                break;
            case MUTATION:
                mutationViewType.setSelected(true);
                break;
        }
    }
}
