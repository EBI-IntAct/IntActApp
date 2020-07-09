package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields;

import java.util.*;
import java.util.stream.Collectors;

public class SummaryEdge extends Edge {
    public final Set<Long> summarizedEdgeSUIDs;

    SummaryEdge(Network network, CyEdge edge) {
        super(network, edge);
        summary = true;
        summarizedEdgeSUIDs = new HashSet<>(EdgeFields.SUMMARY_EDGES_SUID.getValue(edgeRow));
    }

    public List<EvidenceEdge> getSummarizedEdges() {
        return summarizedEdgeSUIDs.stream()
                .map(network::getCyEdge)
                .filter(Objects::nonNull)
                .map(network::getEvidenceEdge)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void updateSummary() {
        summarizedEdgeSUIDs.removeIf(suid -> {
            CyEdge summarizedCyEdge = network.getCyEdge(suid);
            if (summarizedCyEdge == null) return true;
            return network.getEvidenceEdge(summarizedCyEdge) == null;
        });
        EdgeFields.SUMMARY_NB_EDGES.setValue(edgeRow, summarizedEdgeSUIDs.size());
    }
}
