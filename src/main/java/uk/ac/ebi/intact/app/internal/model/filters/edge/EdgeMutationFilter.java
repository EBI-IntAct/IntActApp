package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;

import java.util.List;

public class EdgeMutationFilter extends BooleanFilter<Edge> {
    public EdgeMutationFilter(NetworkView view) {
        super(view, Edge.class, "Mutations", "Hide edges without mutations");
    }

    @Override
    public boolean isToHide(Edge element) {
        for (List<Feature> features : element.getFeatures().values()) {
            for (Feature feature : features) {
                if (FeatureClassifier.mutation.contains(feature.typeIdentifier)) {
                    return false;
                }
            }
        }
        return true;
    }
}
