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
import uk.ac.ebi.intact.app.internal.model.IntactNetwork;
import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.IntactNode;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.app.internal.model.events.IntactNetworkCreatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.IntactNetworkCreatedListener;
import uk.ac.ebi.intact.app.internal.model.events.IntactViewChangedEvent;
import uk.ac.ebi.intact.app.internal.model.events.IntactViewTypeChangedListener;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.CollapseViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.ExpandViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.MutationViewTaskFactory;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.EdgeDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.LegendDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.NodeDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.VersionPanel;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;
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
    final IntactManager manager;

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


    public DetailPanel(final IntactManager manager) {
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


        IntactNetworkView iView = manager.data.getCurrentIntactNetworkView();
        collapsedViewType.addActionListener(e -> manager.utils.execute(collapseViewTaskFactory.createTaskIterator()));
        expandedViewType.addActionListener(e -> manager.utils.execute(expandViewTaskFactory.createTaskIterator()));
        mutationViewType.addActionListener(e -> manager.utils.execute(mutationViewTaskFactory.createTaskIterator()));
        if (iView != null) {
            switch (iView.getType()) {
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
        if (iView != null) {
            setupFilters(iView);
            legendPanel.viewTypeChanged(iView.getType());
        }
        revalidate();
        repaint();
    }


    public void showCytoPanel() {
        // System.out.println("show panel");
        CySwingApplication swingApplication = manager.utils.getService(CySwingApplication.class);
        CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);
        if (!registered) {
            manager.utils.registerService(this, CytoPanelComponent.class, new Properties());
            registered = true;
        }
        if (cytoPanel.getState() == CytoPanelState.HIDE)
            cytoPanel.setState(CytoPanelState.DOCK);

        // Tell tabs
        IntactNetwork currentNetwork = manager.data.getCurrentIntactNetwork();
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
        return "uk.ac.ebi.intact.intactApp.Intact";
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

    private void updateRadioButtons(CyNetworkView view) {
        IntactNetworkView intactNetworkView = manager.data.getIntactNetworkView(view);
        if (intactNetworkView != null) {
            switch (intactNetworkView.getType()) {
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
        IntactNetwork network = manager.data.getIntactNetwork(event.getNetwork());
        if (network != null && ModelUtils.ifHaveIntactNS(network.getNetwork())) {
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

    public void setupFilters(IntactNetworkView view) {
        List<Filter<? extends IntactEdge>> edgeFilters = new ArrayList<>();
        List<Filter<? extends IntactNode>> nodeFilters = new ArrayList<>();
        for (Filter<?> filter : view.getFilters()) {
            if (IntactEdge.class.isAssignableFrom(filter.elementType)) {
                edgeFilters.add((Filter<? extends IntactEdge>) filter);
            } else if (IntactNode.class.isAssignableFrom(filter.elementType)) {
                nodeFilters.add((Filter<? extends IntactNode>) filter);
            }
        }
        edgePanel.setupFilters(edgeFilters);
        nodePanel.setupFilters(nodeFilters);

    }


    @Override
    public void handleEvent(SetCurrentNetworkViewEvent e) {
        CyNetworkView view = e.getNetworkView();
        if (view != null) {
            updateRadioButtons(view);
            legendPanel.networkViewChanged(view);
            edgePanel.networkViewChanged(view);
            IntactNetworkView iView = manager.data.getIntactNetworkView(view);
            if (iView != null) {
                setupFilters(iView);
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
            IntactNetwork newINetwork = event.getNewINetwork();
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
