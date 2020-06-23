package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.filters.ContinuousFilter;

public class EdgeMIScoreFilter extends ContinuousFilter<Edge> {

    public EdgeMIScoreFilter(NetworkView view) {
        super(view, Edge.class, "MI Score", 0, 1);
    }

    @Override
    public double getProperty(Edge element) {
        return element.miScore;
    }
}
