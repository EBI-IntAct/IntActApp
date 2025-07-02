package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryFilters;

import java.util.Map;

public class EdgeInteractionDetectionMethodFilter extends DiscreteFilter<Edge> {

    public EdgeInteractionDetectionMethodFilter(NetworkView view, QueryFilters queryFilters) {
        super(view,
                Edge.class,
                "Interaction detection method", "Method used to determine the interaction",
                queryFilters != null ? queryFilters.getInteractionDetectionMethodsFilter() : null);
    }

    @Override
    public Map<String, String> getPropertyValues(Edge element) {
        return element.getInteractionDetectionMethods();
    }
}
