package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.node.NodeBorderLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.node.NodeColorLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.node.NodeShapeLegendPanel;

import java.awt.*;

public class NodeLegendPanel extends AbstractLegendPanel {
    public final NodeColorLegendPanel nodeColorLegendPanel;
    public final NodeShapeLegendPanel nodeShapeLegendPanel;
    public final NodeBorderLegendPanel nodeBorderLegendPanel;

    public NodeLegendPanel(Manager manager, Network currentINetwork, NetworkView currentIView) {
        super("Nodes", manager, currentINetwork, currentIView);
        content.setBackground(UIColors.lightBackground);
        content.setLayout(new GridBagLayout());
        nodeColorLegendPanel = new NodeColorLegendPanel(manager, currentINetwork, currentIView);
        nodeShapeLegendPanel = new NodeShapeLegendPanel(manager, currentINetwork, currentIView);
        nodeBorderLegendPanel = new NodeBorderLegendPanel(manager, currentINetwork, currentIView);


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
    public void networkChanged(Network newINetwork) {
        super.networkChanged(newINetwork);
        nodeColorLegendPanel.networkChanged(newINetwork);
        nodeShapeLegendPanel.networkChanged(newINetwork);
        nodeBorderLegendPanel.networkChanged(newINetwork);
    }

    @Override
    public void networkViewChanged(NetworkView newINetworkView) {
        super.networkViewChanged(newINetworkView);
        nodeColorLegendPanel.networkViewChanged(newINetworkView);
        nodeShapeLegendPanel.networkViewChanged(newINetworkView);
        nodeBorderLegendPanel.networkViewChanged(newINetworkView);
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
