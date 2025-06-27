package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryFilters;

public class EdgeTypeFilter extends DiscreteFilter<EvidenceEdge> {

    public EdgeTypeFilter(NetworkView view, QueryFilters queryFilters) {
        super(view,
                EvidenceEdge.class,
                "Type",
                "Type of relationship between the connected interactors",
                queryFilters != null ? queryFilters.getInteractionTypesFilter() : null);
    }

    @Override
    public String getPropertyValue(EvidenceEdge element) {
        return element.type.value;
    }
}
