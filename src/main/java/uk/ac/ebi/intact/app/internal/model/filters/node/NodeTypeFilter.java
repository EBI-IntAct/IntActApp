package uk.ac.ebi.intact.app.internal.model.filters.node;

import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.IntactNode;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class NodeTypeFilter extends DiscreteFilter<IntactNode> {
    public NodeTypeFilter(IntactNetworkView iView) {
        super(iView, IntactNode.class, "Type");
    }

    @Override
    public String getPropertyValue(IntactNode node) {
        return node.type;
    }
}
