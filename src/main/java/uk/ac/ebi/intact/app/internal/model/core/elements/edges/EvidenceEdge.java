package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;

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
        type = edgeRow.get(CyEdge.INTERACTION, String.class);
        id = edgeRow.get(ModelUtils.INTACT_ID, Long.class);
        ac = edgeRow.get(ModelUtils.INTACT_AC, String.class);
        detectionMethod = edgeRow.get(ModelUtils.DETECTION_METHOD, String.class);
        hostOrganism = edgeRow.get(ModelUtils.HOST_ORGANISM, String.class);
        hostOrganismTaxId = edgeRow.get(ModelUtils.HOST_ORGANISM_ID, Long.class);
        expansionType = edgeRow.get(ModelUtils.EXPANSION_TYPE, String.class);
        sourceBiologicalRole = edgeRow.get(ModelUtils.SOURCE_BIOLOGICAL_ROLE, String.class);
        targetBiologicalRole = edgeRow.get(ModelUtils.TARGET_BIOLOGICAL_ROLE, String.class);
        pubMedId = edgeRow.get(ModelUtils.PUBMED_ID, String.class);
    }


}
