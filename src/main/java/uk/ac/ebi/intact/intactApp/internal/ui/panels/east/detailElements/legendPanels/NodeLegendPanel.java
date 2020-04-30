package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels;

import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.nodeLegendPanels.NodeBorderLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.nodeLegendPanels.NodeColorLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.nodeLegendPanels.NodeShapeLegendPanel;

import java.awt.*;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.AbstractDetailPanel.backgroundColor;

public class NodeLegendPanel extends AbstractLegendPanel {
    public final NodeColorLegendPanel nodeColorLegendPanel;
    public final NodeShapeLegendPanel nodeShapeLegendPanel;
    public final NodeBorderLegendPanel nodeBorderLegendPanel;

    public NodeLegendPanel(IntactManager manager, IntactNetwork currentINetwork, IntactNetworkView currentIView) {
        super("Nodes", manager, currentINetwork, currentIView);
        content.setBackground(backgroundColor);
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
    public void networkChanged(IntactNetwork newINetwork) {
        super.networkChanged(newINetwork);
        nodeColorLegendPanel.networkChanged(newINetwork);
        nodeShapeLegendPanel.networkChanged(newINetwork);
        nodeBorderLegendPanel.networkChanged(newINetwork);
    }

    @Override
    public void networkViewChanged(IntactNetworkView newINetworkView) {
        super.networkViewChanged(newINetworkView);
        nodeColorLegendPanel.networkViewChanged(newINetworkView);
        nodeShapeLegendPanel.networkViewChanged(newINetworkView);
        nodeBorderLegendPanel.networkViewChanged(newINetworkView);
    }

    public void viewTypeChanged(IntactNetworkView.Type newType) {
        if (currentIView.getType() != IntactNetworkView.Type.MUTATION && newType == IntactNetworkView.Type.MUTATION) {
            content.add(nodeBorderLegendPanel, layoutHelper.down().expandHoriz());
            content.revalidate();
            content.repaint();
        } else if (currentIView.getType() == IntactNetworkView.Type.MUTATION && newType != IntactNetworkView.Type.MUTATION) {
            content.remove(nodeBorderLegendPanel);
            content.revalidate();
            content.repaint();
        }
    }

}
