package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class EdgeExpansionTypeFilter extends DiscreteFilter<IntactEvidenceEdge> {

    public EdgeExpansionTypeFilter(IntactNetworkView iView) {
        super(iView, IntactEvidenceEdge.class, "Expansion type");
    }

    @Override
    public String getPropertyValue(IntactEvidenceEdge element) {
        return element.expansionType != null ? element.expansionType : "not expanded";
    }
}
