package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.app.internal.model.filters.ContinuousFilter;

public class EdgeMIScoreFilter extends ContinuousFilter<IntactEdge> {

    public EdgeMIScoreFilter(IntactNetworkView iView) {
        super(iView, IntactEdge.class, "MI Score", 0, 1);
    }

    @Override
    public double getProperty(IntactEdge element) {
        return element.miScore;
    }
}
