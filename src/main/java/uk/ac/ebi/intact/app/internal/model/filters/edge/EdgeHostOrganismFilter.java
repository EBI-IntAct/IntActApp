package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryFilters;

import java.util.Map;

public class EdgeHostOrganismFilter extends DiscreteFilter<Edge> {

    public EdgeHostOrganismFilter(NetworkView view, QueryFilters queryFilters) {
        super(view,
                Edge.class,
                "Host organism",
                "Experimental environment (cell type, tissue, in vitro…) in which the interaction evidence is captured",
                queryFilters != null ? queryFilters.getInteractionHostOrganismsFilter() : null);
    }

    @Override
    public Map<String, String> getPropertyValues(Edge element) {
        return element.getHostOrganisms();
    }
}
