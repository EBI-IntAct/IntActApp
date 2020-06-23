package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class EdgeDetectionMethodFilter extends DiscreteFilter<IntactEvidenceEdge> {

    public EdgeDetectionMethodFilter(IntactNetworkView iView) {
        super(iView, IntactEvidenceEdge.class, "Detection method");
    }

    @Override
    public String getPropertyValue(IntactEvidenceEdge element) {
        return element.detectionMethod;
    }
}
