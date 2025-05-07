package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.AbstractLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.attributes.EvidenceEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.attributes.MutationEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.attributes.SummaryEdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

public class EdgeLegendPanel extends AbstractLegendPanel {
    private final SummaryEdgeLegendPanel summaryEdgeLegendPanel;
    private final EvidenceEdgeLegendPanel evidenceEdgeLegendPanel;
    private final MutationEdgeLegendPanel mutationEdgeLegendPanel;

    public EdgeLegendPanel(Manager manager, Network currentNetwork, NetworkView currentView) {
        super("Edges", manager, currentNetwork, currentView);

        summaryEdgeLegendPanel = new SummaryEdgeLegendPanel();
        evidenceEdgeLegendPanel = new EvidenceEdgeLegendPanel();
        mutationEdgeLegendPanel = new MutationEdgeLegendPanel();

        content.add(summaryEdgeLegendPanel, new EasyGBC().anchor("west").expandHoriz());
    }

    @Override
    public void filterCurrentLegend() {
        evidenceEdgeLegendPanel.filterLegendWithCurrent(currentView);
    }

    public void viewTypeChanged(NetworkView.Type newType) {

        content.removeAll();
        AbstractEdgeElementPanel newPanel;

        switch (newType) {
            default:
            case SUMMARY:
                newPanel = summaryEdgeLegendPanel;
                break;
            case EVIDENCE:
                newPanel = evidenceEdgeLegendPanel;
                break;
            case MUTATION:
                newPanel = mutationEdgeLegendPanel;
                break;
            case ORTHOLOGY:
                newPanel = evidenceEdgeLegendPanel;
                break;
        }

        content.add(newPanel, new EasyGBC().anchor("west").expandHoriz());
        content.revalidate();
        content.repaint();
    }

}
