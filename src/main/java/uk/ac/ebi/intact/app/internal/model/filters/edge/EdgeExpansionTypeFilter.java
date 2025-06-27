package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryFilters;

public class EdgeExpansionTypeFilter extends BooleanFilter<EvidenceEdge> {
    public EdgeExpansionTypeFilter(NetworkView view, QueryFilters queryFilters) {
        super(view,
                EvidenceEdge.class,
                "Expansion",
                "The method by which complex n-ary data is expanded into binary data.",
                "Hide spoke expanded",
                queryFilters != null ? queryFilters.getExpansionFilter() : null);
    }

    @Override
    public boolean isToHide(EvidenceEdge element) {
        return element.expansionType != null && element.expansionType.equals("spoke expansion");
    }
}
