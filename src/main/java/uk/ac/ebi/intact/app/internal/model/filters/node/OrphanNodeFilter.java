package uk.ac.ebi.intact.app.internal.model.filters.node;

import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OrphanNodeFilter extends Filter<Node> {
    public OrphanNodeFilter(NetworkView view) {
        super(view, "Orphan nodes", "", Node.class);
    }

    @Override
    public void filterView() {
        NetworkView view = getNetworkView();
        Set<CyEdge> visibleEdges = view.visibleEdges.stream().filter(Objects::nonNull).map(edge -> edge.cyEdge).collect(Collectors.toSet());
        view.visibleNodes.removeIf(node -> !CollectionUtils.anyCommonElement(getNetwork().getCyNetwork().getAdjacentEdgeList(node.cyNode, CyEdge.Type.ANY), visibleEdges));
    }
}
