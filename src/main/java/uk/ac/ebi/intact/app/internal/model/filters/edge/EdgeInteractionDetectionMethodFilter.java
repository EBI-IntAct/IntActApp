package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

import java.util.Collection;

public class EdgeInteractionDetectionMethodFilter extends DiscreteFilter<Edge> {

    public EdgeInteractionDetectionMethodFilter(NetworkView view) {
        super(view, Edge.class, "Interaction detection method", "Method used to determine the interaction");
    }

    @Override
    public Collection<String> getPropertyValues(Edge element) {
        return element.getInteractionDetectionMethods();
    }
}
