package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;

import java.util.List;

public class EdgeMutationFilter extends BooleanFilter<Edge> {
    public EdgeMutationFilter(NetworkView view) {
        super(view,
                Edge.class,
                "Mutations",
                "Mutations are defined as changes in a sequence or " +
                        "structure in comparison to a reference entity due to an insertion, deletion or substitution event." +
                        " When possible, the effect of such changes on the specific interaction involved versus the reference" +
                        "(wild type) version of the molecule are reported",
                "Hide edges without mutations");
    }

    @Override
    public boolean isToHide(Edge element) {
        for (List<Feature> features : element.getFeatures().values()) {
            for (Feature feature : features) {
                if (FeatureClassifier.mutation.contains(feature.type.id)) {
                    return false;
                }
            }
        }
        return true;
    }
}
