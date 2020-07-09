package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SummaryEdge extends Edge {
    public final Set<Long> subEdgeSUIDs;
    private final Map<Long, EvidenceEdge> edges = new HashMap<>();

    SummaryEdge(Network network, CyEdge edge) {
        super(network, edge);
        summary = true;
        subEdgeSUIDs = new HashSet<>(EdgeFields.SUMMARY_EDGES_SUID.getValue(edgeRow));
    }

    public Map<Long, EvidenceEdge> getSubEdges() {
        if (subEdgeSUIDs.isEmpty() || !edges.isEmpty()) return edges;

        CyNetwork cyNetwork = network.getCyNetwork();
        for (Long edgeSUID : subEdgeSUIDs) {
            CyEdge cyEdge = cyNetwork.getEdge(edgeSUID);
            if (cyEdge != null) edges.put(edgeSUID, new EvidenceEdge(network, cyEdge));
        }
        return edges;
    }
}
