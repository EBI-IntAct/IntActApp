package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.AbstractEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.CollapsedEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.ExpandedEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.MutationEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

public class EdgeLegendPanel extends AbstractLegendPanel {
    private final CollapsedEdgeLegendPanel collapsedEdgeLegendPanel;
    private final ExpandedEdgeLegendPanel expandedEdgeLegendPanel;
    private final MutationEdgeLegendPanel mutationEdgeLegendPanel;

    public EdgeLegendPanel(Manager manager, Network currentINetwork, NetworkView currentIView) {
        super("Edges", manager, currentINetwork, currentIView);

        collapsedEdgeLegendPanel = new CollapsedEdgeLegendPanel();
        expandedEdgeLegendPanel = new ExpandedEdgeLegendPanel();
        mutationEdgeLegendPanel = new MutationEdgeLegendPanel();

        content.add(collapsedEdgeLegendPanel, new EasyGBC().anchor("west").expandHoriz());
    }

    @Override
    public void filterCurrentLegend() {
    }

    public void viewTypeChanged(NetworkView.Type newType) {

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
