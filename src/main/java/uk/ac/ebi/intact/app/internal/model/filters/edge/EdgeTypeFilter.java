package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryFilters;

import java.util.Collection;

public class EdgeTypeFilter extends DiscreteFilter<Edge> {

    public EdgeTypeFilter(NetworkView view, QueryFilters queryFilters) {
        super(view,
                Edge.class,
                "Type",
                "Type of relationship between the connected interactors",
                queryFilters != null ? queryFilters.getInteractionTypesFilter() : null);
    }

    @Override
    public Collection<String> getPropertyValues(Edge element) {
        return element.getTypes();
    }
}
