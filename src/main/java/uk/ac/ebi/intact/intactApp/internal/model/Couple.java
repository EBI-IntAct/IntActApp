package uk.ac.ebi.intact.intactApp.internal.model;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import uk.ac.ebi.intact.intactApp.internal.utils.CollectionUtils;

import java.util.*;

public class Couple {
    CyNode node1;
    CyNode node2;

    public static Set<Couple> putEdgesToCouples(Collection<CyEdge> edges, Map<Couple, List<CyEdge>> coupleToEdges) {
        Set<Couple> newCouples = new HashSet<>();
        for (CyEdge edge : edges) {
            Couple couple = new Couple(edge);
            newCouples.add(couple);
            CollectionUtils.addToGroups(coupleToEdges, edge, cyEdge -> couple);
        }
        return newCouples;
    }

    Couple(CyEdge edge) {
        this.node1 = edge.getSource();
        this.node2 = edge.getTarget();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Couple)) return false;
        Couple that = (Couple) o;
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