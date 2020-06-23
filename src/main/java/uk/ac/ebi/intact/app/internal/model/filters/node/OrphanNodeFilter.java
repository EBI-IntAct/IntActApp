package uk.ac.ebi.intact.app.internal.model.filters.node;

import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.IntactNode;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;

import java.util.Set;
import java.util.stream.Collectors;

public class OrphanNodeFilter extends Filter<IntactNode> {
    public OrphanNodeFilter(IntactNetworkView iView) {
        super(iView, "Orphan nodes", IntactNode.class);
    }

    @Override
    public void filterView() {
        Set<CyEdge> visibleEdges = iView.visibleEdges.stream().map(edge -> edge.edge).collect(Collectors.toSet());
        iView.visibleNodes.removeIf(node -> !CollectionUtils.anyCommonElement(iNetwork.getNetwork().getAdjacentEdgeList(node.node, CyEdge.Type.ANY), visibleEdges));
    }
}
