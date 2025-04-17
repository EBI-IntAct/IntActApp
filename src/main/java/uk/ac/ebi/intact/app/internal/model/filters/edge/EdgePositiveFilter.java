package uk.ac.ebi.intact.app.internal.model.filters.edge;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;

public class EdgePositiveFilter extends BooleanFilter<Edge> {

    @Setter
    @Getter
    private boolean isNegativeHidden;
    @Setter
    @Getter
    private boolean isPositiveHidden;

    public EdgePositiveFilter(NetworkView networkView) {
        super(networkView,
                Edge.class,
                "Positive Interaction",
                "Positive and/or negative interactions only",
                "Positive and/or negative interactions only");
    }

    @Override
    public boolean isToHide(Edge element) {
        if (element.isNegative){
            return isNegativeHidden;
        } else {
            return isPositiveHidden;
        }
    }
}
