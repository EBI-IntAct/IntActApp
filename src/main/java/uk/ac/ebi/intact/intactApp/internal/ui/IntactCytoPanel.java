package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.*;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.CollapseViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.ExpandViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.MutationViewTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/**
 * Displays information about a protein taken from STRING
 *
 * @author Scooter Morris
 */
public class IntactCytoPanel extends JPanel
        implements CytoPanelComponent2,
        SetCurrentNetworkListener,
        SelectedNodesAndEdgesListener{

    private static final Icon icon = IconUtils.createImageIcon("/IntAct/DIGITAL/Gradient_over_Transparent/favicon_32x32.ico");
    final IntactManager manager;
    private ButtonGroup viewTypes = new ButtonGroup();
    private JPanel viewTypesPanel = new JPanel(new GridLayout(1, 3));
    private JRadioButton collapsedViewType = new JRadioButton("Collapse"),
            expandedViewType = new JRadioButton("Expanded"),
            mutationViewType = new JRadioButton("Mutation");

    private CollapseViewTaskFactory collapseViewTaskFactory;
    private ExpandViewTaskFactory expandViewTaskFactory;
    private MutationViewTaskFactory mutationViewTaskFactory;

    private JTabbedPane tabs;
    private IntactNodePanel nodePanel;
    private IntactEdgePanel edgePanel;
    private boolean registered = false;

    public IntactCytoPanel(final IntactManager manager) {
        this.manager = manager;
        this.setLayout(new BorderLayout());

        collapseViewTaskFactory = new CollapseViewTaskFactory(manager, null);
        expandViewTaskFactory = new ExpandViewTaskFactory(manager, null);
        mutationViewTaskFactory = new MutationViewTaskFactory(manager, null);

        viewTypes.add(collapsedViewType);
        viewTypes.add(expandedViewType);
        viewTypes.add(mutationViewType);

        collapsedViewType.setSelected(true);
        collapsedViewType.addActionListener(e -> manager.execute(collapseViewTaskFactory.createTaskIterator()));
        expandedViewType.addActionListener(e -> manager.execute(expandViewTaskFactory.createTaskIterator()));
        mutationViewType.addActionListener(e -> manager.execute(mutationViewTaskFactory.createTaskIterator()));

        viewTypesPanel.add(collapsedViewType);
        viewTypesPanel.add(expandedViewType);
        viewTypesPanel.add(mutationViewType);
        this.add(viewTypesPanel, BorderLayout.NORTH);


        tabs = new JTabbedPane(JTabbedPane.BOTTOM);
        nodePanel = new IntactNodePanel(manager);
        tabs.add("Nodes", nodePanel);
        edgePanel = new IntactEdgePanel(manager);
        tabs.add("Edges", edgePanel);
        this.add(tabs, BorderLayout.CENTER);
        manager.setCytoPanel(this);
        manager.registerService(this, SetCurrentNetworkListener.class, new Properties());
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
        nodePanel.networkChanged(manager.getCurrentNetwork());
        edgePanel.networkChanged(manager.getCurrentNetwork());
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
        edgePanel.updateSubPanel();
    }

    @Override
    public void handleEvent(SelectedNodesAndEdgesEvent event) {
        if (!registered) return;
        // Pass selected nodes to nodeTab
        nodePanel.selectedNodes(event.getSelectedNodes());
        // Pass selected edges to edgeTab
        edgePanel.selectedEdges(event.getSelectedEdges());
    }

    @Override
    public void handleEvent(SetCurrentNetworkEvent event) {
        CyNetwork network = event.getNetwork();
        if (ModelUtils.ifHaveStringNS(network)) {
            if (!registered) {
                showCytoPanel();
            }

            CyNetworkView view = manager.getCurrentNetworkView();
            switch (manager.getNetworkViewType(view)) {
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

            // Tell tabs
            nodePanel.networkChanged(network);
            edgePanel.networkChanged(network);
        } else {
            hideCytoPanel();
        }
    }

}
