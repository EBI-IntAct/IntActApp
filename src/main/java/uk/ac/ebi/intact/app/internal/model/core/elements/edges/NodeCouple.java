package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import java.util.*;

public class NodeCouple {
    public final CyNode node1;
    public final CyNode node2;

    public static Set<NodeCouple> putEdgesToCouples(Collection<CyEdge> edges, Map<NodeCouple, List<CyEdge>> coupleToEdges) {
        Set<NodeCouple> newCouples = new HashSet<>();
        for (CyEdge edge : edges) {
            NodeCouple couple = new NodeCouple(edge);
            newCouples.add(couple);
            CollectionUtils.addToGroups(coupleToEdges, edge, cyEdge -> couple);
        }
        return newCouples;
    }

    public NodeCouple(CyEdge edge) {
        this.node1 = edge.getSource();
        this.node2 = edge.getTarget();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeCouple)) return false;
        NodeCouple that = (NodeCouple) o;
        return (Objects.equals(node1, that.node1) && Objects.equals(node2, that.node2)) ||
                (Objects.equals(node1, that.node2) && Objects.equals(node2, that.node1));
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1) + Objects.hash(node2);
    }

    @Override
    public String toString() {
        return node1 + " - " + node2;
    }
}