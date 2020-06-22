package uk.ac.ebi.intact.intactApp.internal.model.filters.edge;

import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.filters.ContinuousFilter;

public class EdgeNumberCollapsedFilter extends ContinuousFilter<IntactCollapsedEdge> {

    public EdgeNumberCollapsedFilter(IntactNetworkView iView) {
        super(iView, IntactCollapsedEdge.class, "# Collapsed edges");
    }

    @Override
    public double getProperty(IntactCollapsedEdge element) {
        return element.subEdgeSUIDs.size();
    }
}
