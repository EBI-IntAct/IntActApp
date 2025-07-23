package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;

public class OrphanEdgeFilter extends Filter<Edge> {
    public OrphanEdgeFilter(NetworkView view) {
        super(view, "Orphan edges", "", Edge.class);
    }

    @Override
    public void filterView() {
        NetworkView view = getNetworkView();
        view.getNetwork().getVisibleEvidenceEdges().removeIf(edge ->
                !view.getNetwork().getVisibleNodes().contains(edge.source) || !view.getNetwork().getVisibleNodes().contains(edge.target));
        view.getNetwork().getVisibleSummaryEdges().removeIf(edge ->
                !view.getNetwork().getVisibleNodes().contains(edge.source) || !view.getNetwork().getVisibleNodes().contains(edge.target));
    }

    @Override
    public void reset() {
    }
}
