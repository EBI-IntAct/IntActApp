package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class EdgeInteractionDetectionMethodFilter extends DiscreteFilter<EvidenceEdge> {

    public EdgeInteractionDetectionMethodFilter(NetworkView view) {
        super(view, EvidenceEdge.class, "Interaction detection method", "Method used to determine the interaction");
    }

    @Override
    public String getPropertyValue(EvidenceEdge element) {
        return element.interactionDetectionMethod.value;
    }
}
