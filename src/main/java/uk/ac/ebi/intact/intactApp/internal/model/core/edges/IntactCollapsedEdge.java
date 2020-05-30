package uk.ac.ebi.intact.intactApp.internal.model.core.edges;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IntactCollapsedEdge extends IntactEdge {
    public final Set<Long> subEdgeSUIDs;
    private final Map<Long, IntactEvidenceEdge> edges = new HashMap<>();

    IntactCollapsedEdge(IntactNetwork iNetwork, CyEdge edge) {
        super(iNetwork, edge);
        collapsed = true;
        subEdgeSUIDs = new HashSet<>(edgeRow.getList(ModelUtils.C_INTACT_SUIDS, Long.class));
    }

    public Map<Long, IntactEvidenceEdge> getSubEdges() {
        if (subEdgeSUIDs.isEmpty() || !edges.isEmpty()) return edges;

        CyNetwork network = iNetwork.getNetwork();
        for (Long edgeSUID : subEdgeSUIDs) {
            CyEdge cyEdge = network.getEdge(edgeSUID);
            edges.put(edgeSUID, new IntactEvidenceEdge(iNetwork, cyEdge));
        }
        return edges;
    }
}
