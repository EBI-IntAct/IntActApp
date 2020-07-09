package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SummaryEdge extends Edge {
    public final Set<Long> subEdgeSUIDs;

    SummaryEdge(Network network, CyEdge edge) {
        super(network, edge);
        summary = true;
        subEdgeSUIDs = new HashSet<>(EdgeFields.SUMMARY_EDGES_SUID.getValue(edgeRow));
    }

    public List<EvidenceEdge> getSummarizedEdges() {
        return subEdgeSUIDs.stream()
                .map(network::getCyEdge)
                .filter(Objects::nonNull)
                .map(network::getEvidenceEdge)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
