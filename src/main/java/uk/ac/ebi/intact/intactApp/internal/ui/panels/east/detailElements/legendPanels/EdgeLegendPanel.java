package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels;

import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.edgeLegendPanels.AbstractEdgeLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.edgeLegendPanels.CollapsedEdgeLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.edgeLegendPanels.ExpandedEdgeLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.edgeLegendPanels.MutationEdgeLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

public class EdgeLegendPanel extends AbstractLegendPanel {
    private final CollapsedEdgeLegendPanel collapsedEdgeLegendPanel;
    private final ExpandedEdgeLegendPanel expandedEdgeLegendPanel;
    private final MutationEdgeLegendPanel mutationEdgeLegendPanel;

    public EdgeLegendPanel(IntactManager manager, IntactNetwork currentINetwork, IntactNetworkView currentIView) {
        super("Edges", manager, currentINetwork, currentIView);

        collapsedEdgeLegendPanel = new CollapsedEdgeLegendPanel();
        expandedEdgeLegendPanel = new ExpandedEdgeLegendPanel();
        mutationEdgeLegendPanel = new MutationEdgeLegendPanel();

        content.add(collapsedEdgeLegendPanel, new EasyGBC().anchor("west").expandHoriz());
    }

    @Override
    public void filterCurrentLegend() {
    }

    @Override
    public void networkViewChanged(IntactNetworkView newINetworkView) {
        super.networkViewChanged(newINetworkView);
        viewTypeChanged(newINetworkView.getType());
    }

    public void viewTypeChanged(IntactNetworkView.Type newType) {
        if (currentIView.getType() != newType) {
            content.removeAll();
            AbstractEdgeLegendPanel newPanel;

            switch (newType) {
                default:
                case COLLAPSED:
                    newPanel = collapsedEdgeLegendPanel;
                    break;
                case EXPANDED:
                    newPanel = expandedEdgeLegendPanel;
                    break;
                case MUTATION:
                    newPanel = mutationEdgeLegendPanel;
                    break;
            }

            content.add(newPanel, new EasyGBC().anchor("west").expandHoriz());
            content.revalidate();
            content.repaint();
        }


    }

}
