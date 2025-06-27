package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class EdgeParticipantDetectionMethodFilter extends DiscreteFilter<EvidenceEdge> {

    public EdgeParticipantDetectionMethodFilter(NetworkView view) {
        super(view,
                EvidenceEdge.class,
                "Participant detection method",
                "Method used to determine the identity of the molecules involved in the interaction",
                null);
    }

    @Override
    public String getPropertyValue(EvidenceEdge element) {
        return element.participantDetectionMethod.value;
    }
}
