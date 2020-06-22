package uk.ac.ebi.intact.intactApp.internal.model.filters.node;

import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.filters.Filter;

import java.util.Set;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.intactApp.internal.utils.CollectionUtils.anyCommonElement;

public class OrphanNodeFilter extends Filter<IntactNode> {
    public OrphanNodeFilter(IntactNetworkView iView) {
        super(iView, "Orphan nodes", IntactNode.class);
    }

    @Override
    public void filterView() {
        Set<CyEdge> visibleEdges = iView.visibleEdges.stream().map(edge -> edge.edge).collect(Collectors.toSet());
        iView.visibleNodes.removeIf(node -> !anyCommonElement(iNetwork.getNetwork().getAdjacentEdgeList(node.node, CyEdge.Type.ANY), visibleEdges));
    }
}
