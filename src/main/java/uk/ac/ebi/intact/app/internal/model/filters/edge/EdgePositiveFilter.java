package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;

public class EdgePositiveFilter extends BooleanFilter<Edge> {

    public EdgePositiveFilter(NetworkView view) {
        super(view,
                Edge.class,
                "Positive interactions",
                "Interaction between two molecular components.",
                "Hide positive interactions");
    }

    @Override
    public boolean isToHide(Edge element) {
        return !element.isNegative;
    }
}
