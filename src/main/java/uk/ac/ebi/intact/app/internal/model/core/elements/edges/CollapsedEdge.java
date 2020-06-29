package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.models.EdgeFields;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CollapsedEdge extends Edge {
    public final Set<Long> subEdgeSUIDs;
    private final Map<Long, EvidenceEdge> edges = new HashMap<>();

    CollapsedEdge(Network network, CyEdge edge) {
        super(network, edge);
        collapsed = true;
        subEdgeSUIDs = new HashSet<>(EdgeFields.C_INTACT_SUIDS.getValue(edgeRow));
    }

    public Map<Long, EvidenceEdge> getSubEdges() {
        if (subEdgeSUIDs.isEmpty() || !edges.isEmpty()) return edges;

        CyNetwork cyNetwork = network.getCyNetwork();
        for (Long edgeSUID : subEdgeSUIDs) {
            CyEdge cyEdge = cyNetwork.getEdge(edgeSUID);
            edges.put(edgeSUID, new EvidenceEdge(network, cyEdge));
        }
        return edges;
    }
}
