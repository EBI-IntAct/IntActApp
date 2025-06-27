package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

import java.util.Collection;

public class EdgeParticipantDetectionMethodFilter extends DiscreteFilter<Edge> {

    public EdgeParticipantDetectionMethodFilter(NetworkView view) {
        super(view,
                Edge.class,
                "Participant detection method",
                "Method used to determine the identity of the molecules involved in the interaction",
                null);
    }

    @Override
    public Collection<String> getPropertyValues(Edge element) {
        return element.getParticipantDetectionMethods();
    }
}
