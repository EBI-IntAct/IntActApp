package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.*;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TextIcon;

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
        SelectedNodesAndEdgesListener {

    private static final Icon icon = new TextIcon(IconUtils.LAYERED_STRING_ICON, IconUtils.getIconFont(20.0f), IconUtils.STRING_COLORS, 16, 16);
    final IntactManager manager;
    private JTabbedPane tabs;
    private IntactNodePanel nodePanel;
    private IntactEdgePanel edgePanel;
    private boolean registered = false;

    public IntactCytoPanel(final IntactManager manager) {
        this.manager = manager;
        this.setLayout(new BorderLayout());
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
        return "STRING";
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

            // Tell tabs
            nodePanel.networkChanged(network);
            edgePanel.networkChanged(network);
        } else {
            hideCytoPanel();
        }
    }

}
