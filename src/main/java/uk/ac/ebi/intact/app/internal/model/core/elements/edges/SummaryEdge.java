package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import lombok.Getter;
import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SummaryEdge extends Edge {
    @Getter
    private int nbSummarizedEdges;

    private boolean isNegative;
    private boolean isSpokeExpansion;
    @Getter
    private Map<String, String> hostOrganisms;
    @Getter
    private Map<String, String> interactionDetectionMethods;
    @Getter
    private Map<String, String> participantDetectionMethods;
    @Getter
    private Map<String, String> types;

    SummaryEdge(Network network, CyEdge edge) {
        super(network, edge);
        updateSummary();
    }

    @Override
    public Map<Node, List<Feature>> getFeatures() {
        Map<Node, List<Feature>> features = new HashMap<>();

        Set<Long> presentSummarizedEdgesSUID = getPresentSummarizedEdgesSUID();

        buildFeatures(features, sourceFeatureAcs, source, presentSummarizedEdgesSUID);
        buildFeatures(features, targetFeatureAcs, target, presentSummarizedEdgesSUID);
        return features;
    }

    @Override
    public Boolean isNegative() {
        return isNegative;
    }

    @Override
    public boolean isSpokeExpansion() {
        return isSpokeExpansion;
    }

    protected void buildFeatures(Map<Node, List<Feature>> features, List<String> featureAcs, Node participant, Set<Long> edgesSUID) {
        ArrayList<Feature> participantFeatures = new ArrayList<>();
        features.put(participant, participantFeatures);
        if (participant == null || featureAcs == null) return;
        Network network = getNetwork();
        for (String featureAc : featureAcs) {
            Feature feature = new Feature(network, network.getFeaturesTable().getRow(featureAc));
            if (feature.isPresentIn(edgesSUID)) participantFeatures.add(feature);
        }
    }

    private Stream<Long> getPresentSummarizedEdgesSUIDStream() {
        return EdgeFields.SUMMARIZED_EDGES_SUID.getValue(edgeRow).stream()
                .filter(this::isEvidenceEdgePresentAndVisible);
    }

    private Set<Long> getPresentSummarizedEdgesSUID() {
        return getPresentSummarizedEdgesSUIDStream().collect(Collectors.toSet());
    }

    public List<EvidenceEdge> getSummarizedEdges() {
        return getPresentSummarizedEdgesSUIDStream()
                .map(this::getEvidenceEdge)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void updateSummary() {
        nbSummarizedEdges = (int) getPresentSummarizedEdgesSUIDStream().count();
        isNegative = nbSummarizedEdges > 0 && getSummarizedEdges().stream().allMatch(EvidenceEdge::isNegative);
        isSpokeExpansion = nbSummarizedEdges > 0 && getSummarizedEdges().stream().allMatch(EvidenceEdge::isSpokeExpansion);
        hostOrganisms = new HashMap<>();
        interactionDetectionMethods = new HashMap<>();
        participantDetectionMethods = new HashMap<>();
        types = new HashMap<>();
        for (EvidenceEdge evidenceEdge: getSummarizedEdges()) {
            hostOrganisms.putAll(evidenceEdge.getHostOrganisms());
            interactionDetectionMethods.putAll(evidenceEdge.getInteractionDetectionMethods());
            participantDetectionMethods.putAll(evidenceEdge.getParticipantDetectionMethods());
            types.putAll(evidenceEdge.getTypes());
        }

        EdgeFields.IS_NEGATIVE_INTERACTION.setValue(edgeRow, isNegative());
        EdgeFields.SUMMARY_NB_EDGES.setValue(edgeRow, getNbSummarizedEdges());
    }

    public boolean isSummarizing(EvidenceEdge edge) {
        return EdgeFields.SUMMARIZED_EDGES_SUID.getValue(edgeRow).contains(edge.cyEdge.getSUID());
    }

    @Override
    public String toString() {
        return getSummarizedEdges().toString();
    }

    private boolean isEvidenceEdgePresentAndVisible(Long edgeSuid) {
        return getEvidenceEdge(edgeSuid) != null;
    }

    private EvidenceEdge getEvidenceEdge(Long edgeSuid) {
        Network network = getNetwork();
        CyEdge cyEdge = network.getCyEdge(edgeSuid);
        if (cyEdge != null) {
            EvidenceEdge evidenceEdge = network.getEvidenceEdge(cyEdge);
            if (evidenceEdge != null) {
                if (network.getVisibleEvidenceEdges().contains(evidenceEdge)) {
                    return evidenceEdge;
                }
            }
        }
        return null;
    }
}
