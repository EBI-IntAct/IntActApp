package uk.ac.ebi.intact.intactApp.internal.model.filters.edge;

import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.FeatureClassifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.filters.BooleanFilter;

import java.util.List;

public class EdgeMutationFilter extends BooleanFilter<IntactEdge> {
    public EdgeMutationFilter(IntactNetworkView iView) {
        super(iView, IntactEdge.class, "Mutations", "Hide edges without mutations");
    }

    @Override
    public boolean isToHide(IntactEdge element) {
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
