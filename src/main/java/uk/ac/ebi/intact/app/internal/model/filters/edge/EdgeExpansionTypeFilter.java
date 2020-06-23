package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class EdgeExpansionTypeFilter extends DiscreteFilter<EvidenceEdge> {

    public EdgeExpansionTypeFilter(NetworkView view) {
        super(view, EvidenceEdge.class, "Expansion type");
    }

    @Override
    public String getPropertyValue(EvidenceEdge element) {
        return element.expansionType != null ? element.expansionType : "not expanded";
    }
}
