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
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.events.NetworkCreatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.NetworkCreatedListener;
import uk.ac.ebi.intact.app.internal.model.events.ViewUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.ViewUpdatedListener;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.parameters.OrthologyViewParameterTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.filter.ApplyFiltersTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.filter.ResetFiltersTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.extract.ExtractNetworkViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.EvidenceViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.MutationViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.SummaryViewTaskFactory;
import uk.ac.ebi.intact.app.internal.ui.components.buttons.DocumentedButton;
import uk.ac.ebi.intact.app.internal.ui.components.buttons.HelpButton;
import uk.ac.ebi.intact.app.internal.ui.components.buttons.ToggleSwitch;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.EdgeDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.LegendDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.NodeDetailPanel;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;
import uk.ac.ebi.intact.app.internal.utils.TimeUtils;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

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

    private final ToggleSwitch orthologyButton = new ToggleSwitch(false);

    private final SummaryViewTaskFactory summaryViewTaskFactory;
    private final EvidenceViewTaskFactory evidenceViewTaskFactory;
    private final MutationViewTaskFactory mutationViewTaskFactory;

    private OrthologyViewParameterTaskFactory orthologyViewParameterTaskFactory;

    private final NodeDetailPanel nodePanel;
    private final EdgeDetailPanel edgePanel;
    private final LegendDetailPanel legendPanel;
    private boolean registered;
    private final JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);


    public DetailPanel(final Manager manager) {
        this.manager = manager;

        NetworkView view = manager.data.getCurrentNetworkView();
        Network network = manager.data.getCurrentNetwork();

        manager.utils.registerAllServices(this, new Properties());
        this.setLayout(new BorderLayout());

        summaryViewTaskFactory = new SummaryViewTaskFactory(manager, true);
        evidenceViewTaskFactory = new EvidenceViewTaskFactory(manager, true);
        mutationViewTaskFactory = new MutationViewTaskFactory(manager, true);
        orthologyViewParameterTaskFactory = new OrthologyViewParameterTaskFactory(manager, view, isNetworkGroupedByOrthology(network));

        ButtonGroup viewTypes = new ButtonGroup();
        viewTypes.add(summaryViewType);
        viewTypes.add(evidenceViewType);
        viewTypes.add(mutationViewType);

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

        VerticalPanel viewTypesPanel = new VerticalPanel();



        viewTypesPanel.setBorder(BorderFactory.createTitledBorder("View"));
        VerticalPanel viewsContainer = new VerticalPanel();
        viewsContainer.add(summaryViewType);
        viewsContainer.add(evidenceViewType);
        viewsContainer.add(mutationViewType);
        viewsContainer.setLayout(new GridLayout(3,0));
        viewTypesPanel.add(viewsContainer);

        JPanel viewParamsPanel = getViewParamsPanel();
        viewTypesPanel.add(viewParamsPanel);

        JPanel upperPanel = new JPanel(new GridLayout(1, 2));
        upperPanel.add(viewTypesPanel);

        VerticalPanel buttons = new VerticalPanel();
        buttons.setBorder(BorderFactory.createTitledBorder("Actions"));
        buttons.add(new DocumentedButton(manager,
                "Reset filters",
                "Reset all filters of the current view",
                e -> manager.utils.execute(new ResetFiltersTaskFactory(manager, true).createTaskIterator())));
        buttons.add(new DocumentedButton(manager,
                "Apply filters on tables",
                "Select nodes and edges matching the selected filters",
                e -> manager.utils.execute(new ApplyFiltersTaskFactory(manager, true).createTaskIterator())));
        buttons.add(new DocumentedButton(manager,
                "Extract for analysis",
                "IntAct App uses summary edges in addition of its evidence edges to support its visualisation features.<br>" +
                        "In order to perform topological analysis without any extra edges, you can extract the current view with this button.",
                e -> manager.utils.execute(new ExtractNetworkViewTaskFactory(manager, true).createTaskIterator())));
        upperPanel.add(buttons);
        this.add(upperPanel, BorderLayout.NORTH);

        legendPanel = new LegendDetailPanel(manager);
        tabs.add("Legend", legendPanel);
        nodePanel = new NodeDetailPanel(manager);
        tabs.add("Nodes", nodePanel);
        edgePanel = new EdgeDetailPanel(manager);
        tabs.add("Edges", edgePanel);

        this.add(tabs, BorderLayout.CENTER);
        manager.utils.setDetailPanel(this);
        registered = true;
        if (view != null) {
            setupFilters(view);
            legendPanel.viewUpdated(view.getType());
        }
        revalidate();
        repaint();
    }

    private JPanel getViewParamsPanel() {
        JPanel viewParamsPanel = new JPanel();
        viewParamsPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Align components left

        orthologyButton.setActivated(false);
        orthologyButton.addChangeListener(e -> {
            if (!orthologyButton.isActivated()) {
                orthologyButton.setText("Group by orthology");
            } else {
                orthologyButton.setText("Ungroup by orthology");
            }
            orthologyViewParameterTaskFactory.setParameterApplied(orthologyButton.isActivated());
            manager.utils.execute(orthologyViewParameterTaskFactory.createTaskIterator());
        });

        viewParamsPanel.add(orthologyButton);
        viewParamsPanel.add(new JLabel("Orthology"));
        HelpButton helpButton = new HelpButton(manager, "Create ortholog groups of nodes to compare interactions between species.");
        viewParamsPanel.add(helpButton);

        return viewParamsPanel;
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

    private void updateParamsButton(NetworkView networkView) {
        Network network = networkView.getNetwork();
        boolean isGrouped = network != null && isNetworkGroupedByOrthology(network);
        orthologyViewParameterTaskFactory = new OrthologyViewParameterTaskFactory(manager, networkView, isNetworkGroupedByOrthology(network));
        orthologyButton.setActivated(isGrouped);
        orthologyButton.setText(isGrouped ? "Ungroup by orthology" : "Group by orthology");
    }

    private boolean isNetworkGroupedByOrthology(Network network) {
        return !network.getGroupManager().getGroupSet(network.getCyNetwork()).isEmpty();
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
                updateParamsButton(view);
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
