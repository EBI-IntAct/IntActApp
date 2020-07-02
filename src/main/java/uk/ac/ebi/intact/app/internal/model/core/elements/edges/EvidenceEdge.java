package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;

import static uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields.*;

public class EvidenceEdge extends Edge {
    public final String ac;
    public final CVTerm type;
    public final CVTerm interactionDetectionMethod;
    public final CVTerm participantDetectionMethod;
    public final String hostOrganism;
    public final String expansionType;
    public final String pubMedId;
    public final CVTerm sourceBiologicalRole;
    public final CVTerm sourceExperimentalRole;
    public final CVTerm targetBiologicalRole;
    public final CVTerm targetExperimentalRole;
    public final long hostOrganismTaxId;
    public final long id;

    EvidenceEdge(Network network, CyEdge edge) {
        super(network, edge);
        ac = AC.getValue(edgeRow);
        type = new CVTerm(edgeRow, TYPE, TYPE_MI_ID, SourceOntology.MI);
        id = ID.getValue(edgeRow);
        interactionDetectionMethod = new CVTerm(edgeRow, INTERACTION_DETECTION_METHOD, INTERACTION_DETECTION_METHOD_MI_ID, SourceOntology.MI);
        participantDetectionMethod = new CVTerm(edgeRow, PARTICIPANT_DETECTION_METHOD, PARTICIPANT_DETECTION_METHOD_MI_ID, SourceOntology.MI);
        hostOrganism = HOST_ORGANISM.getValue(edgeRow);
        hostOrganismTaxId = HOST_ORGANISM_ID.getValue(edgeRow);
        expansionType = EXPANSION_TYPE.getValue(edgeRow);

        sourceBiologicalRole = new CVTerm(edgeRow, SOURCE_BIOLOGICAL_ROLE, SOURCE_BIOLOGICAL_ROLE_MI_ID, SourceOntology.MI);
        sourceExperimentalRole = new CVTerm(edgeRow, SOURCE_EXPERIMENTAL_ROLE, SOURCE_EXPERIMENTAL_ROLE_MI_ID, SourceOntology.MI);

        targetBiologicalRole = new CVTerm(edgeRow, TARGET_BIOLOGICAL_ROLE, TARGET_BIOLOGICAL_ROLE_MI_ID, SourceOntology.MI);
        targetExperimentalRole = new CVTerm(edgeRow, TARGET_EXPERIMENTAL_ROLE, TARGET_EXPERIMENTAL_ROLE_MI_ID, SourceOntology.MI);

        pubMedId = PUBMED_ID.getValue(edgeRow);
        summary = false;
    }


}
