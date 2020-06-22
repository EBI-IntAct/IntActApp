package uk.ac.ebi.intact.intactApp.internal.model.filters.edge;

import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.model.filters.DiscreteFilter;

public class EdgeTypeFilter extends DiscreteFilter<IntactEvidenceEdge> {

    public EdgeTypeFilter(IntactNetworkView iView) {
        super(iView, IntactEvidenceEdge.class, "Type");
    }

    @Override
    public String getPropertyValue(IntactEvidenceEdge element) {
        return element.type;
    }
}
