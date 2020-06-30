package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;

import static uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields.*;

public class EvidenceEdge extends Edge {
    public final String type;
    public final String ac;
    public final String detectionMethod;
    public final String hostOrganism;
    public final String expansionType;
    public final String pubMedId;
    public final String sourceBiologicalRole;
    public final String targetBiologicalRole;
    public final long hostOrganismTaxId;
    public final long id;

    EvidenceEdge(Network network, CyEdge edge) {
        super(network, edge);
        collapsed = false;
        type = TYPE.getValue(edgeRow);
        id = ID.getValue(edgeRow);
        ac = AC.getValue(edgeRow);
        detectionMethod = DETECTION_METHOD.getValue(edgeRow);
        hostOrganism = HOST_ORGANISM.getValue(edgeRow);
        hostOrganismTaxId = HOST_ORGANISM_ID.getValue(edgeRow);
        expansionType = EXPANSION_TYPE.getValue(edgeRow);
        sourceBiologicalRole = SOURCE_BIOLOGICAL_ROLE.getValue(edgeRow);
        targetBiologicalRole = TARGET_BIOLOGICAL_ROLE.getValue(edgeRow);
        pubMedId = PUBMED_ID.getValue(edgeRow);
    }


}
