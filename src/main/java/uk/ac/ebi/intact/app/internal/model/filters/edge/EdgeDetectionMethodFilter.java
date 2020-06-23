package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class EdgeDetectionMethodFilter extends DiscreteFilter<EvidenceEdge> {

    public EdgeDetectionMethodFilter(NetworkView view) {
        super(view, EvidenceEdge.class, "Detection method");
    }

    @Override
    public String getPropertyValue(EvidenceEdge element) {
        return element.detectionMethod;
    }
}
