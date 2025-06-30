package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.FeatureFields;

import java.util.*;

import static uk.ac.ebi.intact.app.internal.model.managers.Manager.INTACT_GRAPH_WS;
import static uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields.*;

public class EvidenceEdge extends Edge {
    public final long id;
    public final String ac;
    public final CVTerm type;
    public final CVTerm interactionDetectionMethod;
    public final CVTerm participantDetectionMethod;
    public final String hostOrganism;
    public final String hostOrganismTaxId;
    public final String expansionType;
    public final String pubMedId;
    public final CVTerm sourceBiologicalRole;
    public final CVTerm sourceExperimentalRole;
    public final CVTerm targetBiologicalRole;
    public final CVTerm targetExperimentalRole;
    private JsonNode detailsJSON;
    public final Boolean isNegative;

    EvidenceEdge(Network network, CyEdge edge) {
        super(network, edge);
        ac = AC.getValue(edgeRow);
        type = new CVTerm(edgeRow, TYPE);
        id = ID.getValue(edgeRow) != null ? ID.getValue(edgeRow) : -1L;
        interactionDetectionMethod = new CVTerm(edgeRow, INTERACTION_DETECTION_METHOD);
        participantDetectionMethod = new CVTerm(edgeRow, PARTICIPANT_DETECTION_METHOD);
        hostOrganism = HOST_ORGANISM.getValue(edgeRow);
        hostOrganismTaxId = HOST_ORGANISM_ID.getValue(edgeRow);
        expansionType = EXPANSION_TYPE.getValue(edgeRow);

        sourceBiologicalRole = new CVTerm(edgeRow, BIOLOGICAL_ROLE.SOURCE);
        sourceExperimentalRole = new CVTerm(edgeRow, EXPERIMENTAL_ROLE.SOURCE);

        targetBiologicalRole = new CVTerm(edgeRow, BIOLOGICAL_ROLE.TARGET);
        targetExperimentalRole = new CVTerm(edgeRow, EXPERIMENTAL_ROLE.TARGET);

        pubMedId = PUBMED_ID.getValue(edgeRow);
        isNegative = IS_NEGATIVE_INTERACTION.getValue(edgeRow);
    }

    @Override
    public Map<Node, List<Feature>> getFeatures() {
        Map<Node, List<Feature>> features = new HashMap<>();

        buildFeatures(features, sourceFeatureAcs, source);
        buildFeatures(features, targetFeatureAcs, target);
        return features;
    }

    @Override
    public boolean isNegative() {
        return isNegative;
    }

    @Override
    public boolean isSpokeExpansion() {
        return expansionType != null && expansionType.equals("spoke expansion");
    }

    @Override
    public Collection<String> getHostOrganisms() {
        return List.of(hostOrganism);
    }

    @Override
    public Collection<String> getInteractionDetectionMethods() {
        return List.of(interactionDetectionMethod.value);
    }

    @Override
    public Collection<String> getParticipantDetectionMethods() {
        return List.of(participantDetectionMethod.value);
    }

    @Override
    public Collection<String> getTypes() {
        return List.of(type.value);
    }

    protected void buildFeatures(Map<Node, List<Feature>> features, List<String> featureAcs, Node participant) {
        ArrayList<Feature> participantFeatures = new ArrayList<>();
        features.put(participant, participantFeatures);
        if (participant == null || featureAcs == null) return;
        Network network = getNetwork();
        for (String featureAc : featureAcs) {
            participantFeatures.add(new Feature(network, network.getFeaturesTable().getRow(featureAc)));
        }
    }

    public boolean isAffectedByMutation() {
        return getFeatures().values().stream()
                .flatMap(List::stream)
                .anyMatch(feature -> FeatureClassifier.mutation.contains(feature.type.id));
    }

    private JsonNode getDetailsJSON() {
        if (detailsJSON != null && !detailsJSON.isNull()) return detailsJSON;
        detailsJSON = HttpUtils.getJSON(INTACT_GRAPH_WS + "network/edge/details/" + ac, new HashMap<>(), getNetwork().manager);
        return detailsJSON;
    }

    public JsonNode getAnnotations() {
        JsonNode detailsJSON = getDetailsJSON();
        return (detailsJSON != null) ? detailsJSON.get("annotations") : null;
    }

    public JsonNode getParameters() {
        JsonNode detailsJSON = getDetailsJSON();
        return (detailsJSON != null) ? detailsJSON.get("parameters") : null;
    }

    public EvidenceEdge cloneInto(Network network) {
        CyNetwork cyNetwork = network.getCyNetwork();
        CyEdge edge = cyNetwork.addEdge(source.cyNode, target.cyNode, false);
        CyRow row = cyNetwork.getRow(edge);
        AC.setValue(row, ac);
        ID.setValue(row, id);
        NAME.setValue(row, name);
        MI_SCORE.setValue(row, miScore);
        type.writeInTable(row, TYPE);
        interactionDetectionMethod.writeInTable(row, INTERACTION_DETECTION_METHOD);
        participantDetectionMethod.writeInTable(row, PARTICIPANT_DETECTION_METHOD);

        HOST_ORGANISM.setValue(row, hostOrganism);
        HOST_ORGANISM_ID.setValue(row, hostOrganismTaxId);
        EXPANSION_TYPE.setValue(row, expansionType);

        sourceBiologicalRole.writeInTable(row, BIOLOGICAL_ROLE.SOURCE);
        sourceExperimentalRole.writeInTable(row, EXPERIMENTAL_ROLE.SOURCE);
        FEATURES.SOURCE.setValue(row, sourceFeatureAcs);

        targetBiologicalRole.writeInTable(row, BIOLOGICAL_ROLE.TARGET);
        targetExperimentalRole.writeInTable(row, EXPERIMENTAL_ROLE.TARGET);
        FEATURES.TARGET.setValue(row, targetFeatureAcs);

        CyTable featuresTable = network.getFeaturesTable();
        sourceFeatureAcs.forEach(pk -> FeatureFields.EDGES_SUID.getValue(featuresTable.getRow(pk)).add(edge.getSUID()));
        targetFeatureAcs.forEach(pk -> FeatureFields.EDGES_SUID.getValue(featuresTable.getRow(pk)).add(edge.getSUID()));
        AFFECTED_BY_MUTATION.setValue(row, isAffectedByMutation());

        PUBMED_ID.setValue(row, pubMedId);

        return (EvidenceEdge) Edge.createEdge(network, edge);
    }

    @Override
    public String toString() {
        return "EvidenceEdge{" +
                "ac='" + ac + '\'' +
                '}';
    }
}
