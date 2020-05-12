package uk.ac.ebi.intact.intactApp.internal.ui.panels.east;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.*;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactNetworkCreatedEvent;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactNetworkCreatedListener;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactViewTypeChangedEvent;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactViewTypeChangedListener;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.CollapseViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.ExpandViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.MutationViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.EdgeDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.LegendDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.NodeDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TimeUtils;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
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
    private boolean registered = false;



    public DetailPanel(final IntactManager manager) {
        this.manager = manager;
        manager.addIntactNetworkCreatedListener(this);
        manager.addIntactViewTypeChangedListener(this);
        this.setLayout(new BorderLayout());

        collapseViewTaskFactory = new CollapseViewTaskFactory(manager);
        expandViewTaskFactory = new ExpandViewTaskFactory(manager);
        mutationViewTaskFactory = new MutationViewTaskFactory(manager);

        ButtonGroup viewTypes = new ButtonGroup();
        viewTypes.add(collapsedViewType);
        viewTypes.add(expandedViewType);
        viewTypes.add(mutationViewType);

        collapsedViewType.setSelected(true);
        collapsedViewType.addActionListener(e -> manager.execute(collapseViewTaskFactory.createTaskIterator()));
        expandedViewType.addActionListener(e -> manager.execute(expandViewTaskFactory.createTaskIterator()));
        mutationViewType.addActionListener(e -> manager.execute(mutationViewTaskFactory.createTaskIterator()));

        JPanel viewTypesPanel = new JPanel(new GridLayout(3, 1));
        viewTypesPanel.setBorder(BorderFactory.createTitledBorder("View types"));
        viewTypesPanel.add(collapsedViewType);
        viewTypesPanel.add(expandedViewType);
        viewTypesPanel.add(mutationViewType);
        JPanel upperPanel = new JPanel(new GridLayout(1, 2));
        upperPanel.add(viewTypesPanel);

//        ToggleSwitch toggleFancy = new ToggleSwitch(true, new Color(59, 136, 253));
//        toggleFancy.addChangeListener(e -> manager.toggleFancyStyles());
//        JPanel buttonsPanel = new JPanel();
//        Box fastFancyBox = Box.createHorizontalBox();
//        fastFancyBox.add(new JLabel("Fast"));
//        fastFancyBox.add(Box.createHorizontalStrut(4));
//        fastFancyBox.add(toggleFancy);
//        fastFancyBox.add(Box.createHorizontalStrut(4));
//        fastFancyBox.add(new JLabel("Fancy"));
//        buttonsPanel.add(fastFancyBox);
//        upperPanel.add(buttonsPanel);
        this.add(upperPanel, BorderLayout.NORTH);


        JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);
        nodePanel = new NodeDetailPanel(manager);
        tabs.add("Nodes", nodePanel);
        edgePanel = new EdgeDetailPanel(manager);
        tabs.add("Edges", edgePanel);
        legendPanel = new LegendDetailPanel(manager);
        tabs.add("Legend", legendPanel);
        tabs.getComponent(0).setBackground(Color.WHITE);
        this.add(tabs, BorderLayout.CENTER);
        manager.setCytoPanel(this);
        manager.registerService(this, SetCurrentNetworkListener.class, new Properties());
        manager.registerService(this, SetCurrentNetworkViewListener.class, new Properties());
        manager.registerService(this, SelectedNodesAndEdgesListener.class, new Properties());
        registered = true;
        revalidate();
        repaint();
    }


    public void showCytoPanel() {
        // System.out.println("show panel");
        CySwingApplication swingApplication = manager.getService(CySwingApplication.class);
        CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);
        if (!registered) {
            manager.registerService(this, CytoPanelComponent.class, new Properties());
            registered = true;
        }
        if (cytoPanel.getState() == CytoPanelState.HIDE)
            cytoPanel.setState(CytoPanelState.DOCK);

        // Tell tabs

        IntactNetwork currentNetwork = manager.getCurrentIntactNetwork();
        nodePanel.networkChanged(currentNetwork);
        edgePanel.networkChanged(currentNetwork);
        legendPanel.networkChanged(currentNetwork);
    }

    public void hideCytoPanel() {
        manager.unregisterService(this, CytoPanelComponent.class);
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

            new Thread(() -> nodePanel.selectedNodes(event.getSelectedNodes())).start();
            new Thread(() -> edgePanel.selectedEdges(event.getSelectedEdges())).start();
        }
    }

    private void updateRadioButtons(CyNetworkView view) {
        IntactNetworkView intactNetworkView = manager.getIntactNetworkView(view);
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
        IntactNetwork network = manager.getIntactNetwork(event.getNetwork());
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


    @Override
    public void handleEvent(SetCurrentNetworkViewEvent e) {
        CyNetworkView view = e.getNetworkView();
        if (view != null) {
            updateRadioButtons(view);
            legendPanel.networkViewChanged(view);
            edgePanel.networkViewChanged(view);
        }
    }

    @Override
    public void handleEvent(IntactNetworkCreatedEvent event) {
        if (!registered) {
            showCytoPanel();
        }
        if (nodePanel != null) {
            // Tell tabs
            nodePanel.networkChanged(event.getNewINetwork());
            edgePanel.networkChanged(event.getNewINetwork());
            legendPanel.networkChanged(event.getNewINetwork());
        }
    }

    @Override
    public void handleEvent(IntactViewTypeChangedEvent event) {
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
