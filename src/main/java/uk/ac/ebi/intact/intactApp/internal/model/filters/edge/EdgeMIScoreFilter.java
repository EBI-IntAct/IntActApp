package uk.ac.ebi.intact.intactApp.internal.model.filters.edge;

import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.filters.ContinuousFilter;

public class EdgeMIScoreFilter extends ContinuousFilter<IntactEdge> {

    public EdgeMIScoreFilter(IntactNetworkView iView) {
        super(iView, IntactEdge.class, "MI Score", 0, 1);
    }

    @Override
    public double getProperty(IntactEdge element) {
        return element.miScore;
    }
}
