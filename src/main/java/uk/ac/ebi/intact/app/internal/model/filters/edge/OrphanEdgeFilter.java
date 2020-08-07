package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;

public class OrphanEdgeFilter extends Filter<Edge> {
    public OrphanEdgeFilter(NetworkView view) {
        super(view, "Orphan edges", Edge.class);
    }

    @Override
    public void filterView() {
        view.visibleEdges.removeIf(edge -> !view.visibleNodes.contains(edge.source) || !view.visibleNodes.contains(edge.target));
    }
}
