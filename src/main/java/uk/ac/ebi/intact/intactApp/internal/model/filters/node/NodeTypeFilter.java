package uk.ac.ebi.intact.intactApp.internal.model.filters.node;

import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.filters.DiscreteFilter;

public class NodeTypeFilter extends DiscreteFilter<IntactNode> {
    public NodeTypeFilter(IntactNetworkView iView) {
        super(iView, IntactNode.class, "Type");
    }

    @Override
    public String getPropertyValue(IntactNode node) {
        return node.type;
    }
}
