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
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.CollapseViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.ExpandViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.MutationViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.ui.components.ToggleSwitch;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.EdgeDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.LegendDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.NodeDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/**
 * Displays information about a protein taken from IntAct
 *
 * @author Scooter Morris
 */
public class DetailPanel extends JPanel
        implements CytoPanelComponent2,
        SetCurrentNetworkListener,
        SetCurrentNetworkViewListener,
        SelectedNodesAndEdgesListener {

    private static final Icon icon = IconUtils.createImageIcon("/IntAct/DIGITAL/Gradient_over_Transparent/favicon_32x32.ico");
    final IntactManager manager;
    private ButtonGroup viewTypes = new ButtonGroup();
    private JPanel viewTypesPanel = new JPanel(new GridLayout(3, 1));
    private JPanel upperPanel = new JPanel(new GridLayout(1, 2));

    private JRadioButton collapsedViewType = new JRadioButton("Collapse"),
            expandedViewType = new JRadioButton("Expanded"),
            mutationViewType = new JRadioButton("Mutation");

    private CollapseViewTaskFactory collapseViewTaskFactory;
    private ExpandViewTaskFactory expandViewTaskFactory;
    private MutationViewTaskFactory mutationViewTaskFactory;

    private JTabbedPane tabs;
    private NodeDetailPanel nodePanel;
    private EdgeDetailPanel edgePanel;
    private LegendDetailPanel legendPanel;
    private boolean registered = false;

    public DetailPanel(final IntactManager manager) {
        this.manager = manager;
        this.setLayout(new BorderLayout());

        collapseViewTaskFactory = new CollapseViewTaskFactory(manager);
        expandViewTaskFactory = new ExpandViewTaskFactory(manager);
        mutationViewTaskFactory = new MutationViewTaskFactory(manager);

        viewTypes.add(collapsedViewType);
        viewTypes.add(expandedViewType);
        viewTypes.add(mutationViewType);

        collapsedViewType.setSelected(true);
        collapsedViewType.addActionListener(e -> {
            manager.execute(collapseViewTaskFactory.createTaskIterator());
            legendPanel.viewTypeChanged(IntactNetworkView.Type.COLLAPSED);
        });
        expandedViewType.addActionListener(e -> {
            manager.execute(expandViewTaskFactory.createTaskIterator());
            legendPanel.viewTypeChanged(IntactNetworkView.Type.EXPANDED);
        });
        mutationViewType.addActionListener(e -> {
            manager.execute(mutationViewTaskFactory.createTaskIterator());
            legendPanel.viewTypeChanged(IntactNetworkView.Type.MUTATION);
        });

        viewTypesPanel.setBorder(BorderFactory.createTitledBorder("View types"));
        viewTypesPanel.add(collapsedViewType);
        viewTypesPanel.add(expandedViewType);
        viewTypesPanel.add(mutationViewType);
        upperPanel.add(viewTypesPanel);

        ToggleSwitch toggleFancy = new ToggleSwitch(true, new Color(59, 136, 253));
        toggleFancy.addChangeListener(e -> manager.toggleFancyStyles());
        JPanel buttonsPanel = new JPanel();
        Box fastFancyBox = Box.createHorizontalBox();
        fastFancyBox.add(new JLabel("Fast"));
        fastFancyBox.add(Box.createHorizontalStrut(4));
        fastFancyBox.add(toggleFancy);
        fastFancyBox.add(Box.createHorizontalStrut(4));
        fastFancyBox.add(new JLabel("Fancy"));
        buttonsPanel.add(fastFancyBox);
        upperPanel.add(buttonsPanel);
        this.add(upperPanel, BorderLayout.NORTH);


        tabs = new JTabbedPane(JTabbedPane.BOTTOM);
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

    public void updateControls() {
        nodePanel.updateControls();
    }

    @Override
    public void handleEvent(SelectedNodesAndEdgesEvent event) {
        if (!registered) return;
        // Pass selected nodes to nodeTab
        nodePanel.selectedNodes(event.getSelectedNodes());
        // Pass selected edges to edgeTab
        edgePanel.selectedEdges(event.getSelectedEdges());
    }

    private void updateRadioButtons(CyNetworkView view) {
        switch (manager.getIntactNetworkView(view).getType()) {
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

    @Override
    public void handleEvent(SetCurrentNetworkEvent event) {
        IntactNetwork network = manager.getIntactNetwork(event.getNetwork());
        if (ModelUtils.ifHaveIntactNS(network.getNetwork())) {
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

}
