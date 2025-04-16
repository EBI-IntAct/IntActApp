package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;

public class EdgeNegativeFilter extends BooleanFilter<Edge> {

        public EdgeNegativeFilter(NetworkView view) {
            super(view,
                    Edge.class,
                    "Negative interactions",
                    "Negation is used for annotation of experiments that demonstrate" +
                            " that an interaction does not occur.",
                    "Hide negative interactions");
        }

        @Override
        public boolean isToHide(Edge element) {
            return element.isNegative;
        }
}

