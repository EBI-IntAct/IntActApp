package uk.ac.ebi.intact.app.internal.model.filters.node;

import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.IntactNode;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class NodeSpeciesFilter extends DiscreteFilter<IntactNode> {
    public NodeSpeciesFilter(IntactNetworkView iView) {
        super(iView, IntactNode.class, "Species");
    }

    @Override
    public String getPropertyValue(IntactNode element) {
        return element.species;
    }
}
