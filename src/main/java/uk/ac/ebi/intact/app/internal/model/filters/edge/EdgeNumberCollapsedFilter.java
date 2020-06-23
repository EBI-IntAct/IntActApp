package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.CollapsedEdge;
import uk.ac.ebi.intact.app.internal.model.filters.ContinuousFilter;

public class EdgeNumberCollapsedFilter extends ContinuousFilter<CollapsedEdge> {

    public EdgeNumberCollapsedFilter(NetworkView view) {
        super(view, CollapsedEdge.class, "# Collapsed edges");
    }

    @Override
    public double getProperty(CollapsedEdge element) {
        return element.subEdgeSUIDs.size();
    }
}
