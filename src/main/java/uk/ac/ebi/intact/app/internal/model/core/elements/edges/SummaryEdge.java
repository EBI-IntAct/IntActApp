package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import lombok.Getter;
import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields;

import java.util.*;
import java.util.stream.Collectors;

public class SummaryEdge extends Edge {
    private int nbSummarizedEdges;

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

    public Boolean isNegative() {
        List<EvidenceEdge> summarizedEdges = getSummarizedEdges();
        for (EvidenceEdge edge : summarizedEdges) {
            if (edge.isNegativeInteraction){
                return true;
            }
        }
        return false;
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

    private Set<Long> getPresentSummarizedEdgesSUID() {
        Network network = getNetwork();
        return EdgeFields.SUMMARIZED_EDGES_SUID.getValue(edgeRow).stream()
                .filter(suid -> network.getCyEdge(suid) != null)
                .collect(Collectors.toSet());
    }

    public List<EvidenceEdge> getSummarizedEdges() {
        Network network = getNetwork();
        return EdgeFields.SUMMARIZED_EDGES_SUID.getValue(edgeRow).stream()
                .map(network::getCyEdge)
                .filter(Objects::nonNull)
                .map(network::getEvidenceEdge)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void updateSummary() {
        Network network = getNetwork();
        nbSummarizedEdges = (int) EdgeFields.SUMMARIZED_EDGES_SUID.getValue(edgeRow).stream()
                .filter(suid -> {
                    CyEdge summarizedCyEdge = network.getCyEdge(suid);
                    if (summarizedCyEdge == null) return false;
                    return network.getEvidenceEdge(summarizedCyEdge) != null;
                }).count();
        EdgeFields.IS_NEGATIVE_INTERACTION.setValue(edgeRow, isNegative());
        EdgeFields.SUMMARY_NB_EDGES.setValue(edgeRow, nbSummarizedEdges);
    }

    public boolean isSummarizing(EvidenceEdge edge) {
        return EdgeFields.SUMMARIZED_EDGES_SUID.getValue(edgeRow).contains(edge.cyEdge.getSUID());
    }

    public int getNbSummarizedEdges() {
        return nbSummarizedEdges;
    }

    @Override
    public String toString() {
        return getSummarizedEdges().toString();
    }
}
