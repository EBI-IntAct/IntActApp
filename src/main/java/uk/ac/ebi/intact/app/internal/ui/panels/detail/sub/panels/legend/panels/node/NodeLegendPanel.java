package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.node;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.AbstractLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.node.elements.NodeBorderLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.node.elements.NodeColorLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.node.elements.NodeShapeLegendPanel;

import java.awt.*;

public class NodeLegendPanel extends AbstractLegendPanel {
    public final NodeColorLegendPanel nodeColorLegendPanel;
    public final NodeShapeLegendPanel nodeShapeLegendPanel;
    public final NodeBorderLegendPanel nodeBorderLegendPanel;

    public NodeLegendPanel(Manager manager, Network currentNetwork, NetworkView currentView) {
        super("Nodes", manager, currentNetwork, currentView);
        content.setBackground(UIColors.lightBackground);
        content.setLayout(new GridBagLayout());
        nodeColorLegendPanel = new NodeColorLegendPanel(manager, currentNetwork, currentView);
        nodeShapeLegendPanel = new NodeShapeLegendPanel(manager, currentNetwork, currentView);
        nodeBorderLegendPanel = new NodeBorderLegendPanel(manager, currentNetwork, currentView);


        content.add(nodeColorLegendPanel, layoutHelper.expandHoriz());
        content.add(nodeShapeLegendPanel, layoutHelper.down().expandHoriz());
    }

    @Override
    public void filterCurrentLegend() {
        nodeColorLegendPanel.filterCurrentLegend();
        nodeShapeLegendPanel.filterCurrentLegend();
        nodeBorderLegendPanel.filterCurrentLegend();
    }

    @Override
    public void networkChanged(Network newNetwork) {
        super.networkChanged(newNetwork);
        nodeColorLegendPanel.networkChanged(newNetwork);
        nodeShapeLegendPanel.networkChanged(newNetwork);
        nodeBorderLegendPanel.networkChanged(newNetwork);
    }

    @Override
    public void networkViewChanged(NetworkView newNetworkView) {
        super.networkViewChanged(newNetworkView);
        nodeColorLegendPanel.networkViewChanged(newNetworkView);
        nodeShapeLegendPanel.networkViewChanged(newNetworkView);
        nodeBorderLegendPanel.networkViewChanged(newNetworkView);
    }

    public void viewTypeChanged(NetworkView.Type newType) {
        if (newType == NetworkView.Type.MUTATION) {
            content.add(nodeBorderLegendPanel, layoutHelper.down().expandHoriz());
            content.revalidate();
            content.repaint();
        } else {
            content.remove(nodeBorderLegendPanel);
            content.revalidate();
            content.repaint();
        }
    }

}
