package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels;

import uk.ac.ebi.intact.app.internal.model.IntactNetwork;
import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.AbstractEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.CollapsedEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.ExpandedEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.MutationEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

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

    public void viewTypeChanged(IntactNetworkView.Type newType) {

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
