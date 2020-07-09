package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields;

import java.util.*;

import static uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields.SOURCE_FEATURES;
import static uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields.TARGET_FEATURES;

public abstract class Edge implements Element {
    public final Network network;
    public final CyEdge cyEdge;
    public final String name;
    public boolean summary;
    public final CyRow edgeRow;
    public final Node source;
    public final Node target;
    public final double miScore;
    public final List<String> sourceFeatureAcs;
    public final List<String> targetFeatureAcs;


    public static Edge createEdge(Network network, CyEdge edge) {
        if (network == null || edge == null) return null;
        CyRow edgeRow = network.getCyNetwork().getRow(edge);
        if (edgeRow == null) return null;
        Boolean isSummarized = EdgeFields.IS_SUMMARY.getValue(edgeRow);
        if (isSummarized == null) return null;
        if (isSummarized) {
            return new SummaryEdge(network, edge);
        } else {
            return new EvidenceEdge(network, edge);
        }
    }


    Edge(Network network, CyEdge cyEdge) {
        this.network = network;
        this.cyEdge = cyEdge;
        edgeRow = network.getCyNetwork().getRow(cyEdge);

        name = edgeRow.get(CyNetwork.NAME, String.class);
        miScore = EdgeFields.MI_SCORE.getValue(edgeRow);
        source = new Node(network, cyEdge.getSource());
        target = cyEdge.getTarget() != null ? new Node(network, cyEdge.getTarget()) : null;

        sourceFeatureAcs = SOURCE_FEATURES.getValue(edgeRow);
        if (sourceFeatureAcs != null) {
            sourceFeatureAcs.removeIf(String::isBlank);
        }
        targetFeatureAcs = TARGET_FEATURES.getValue(edgeRow);
        if (targetFeatureAcs != null) {
            targetFeatureAcs.removeIf(String::isBlank);
        }
    }

    public Map<Node, List<Feature>> getFeatures() {
        Map<Node, List<Feature>> features = new HashMap<>();

        buildFeatures(features, sourceFeatureAcs, source);
        buildFeatures(features, targetFeatureAcs, target);
        return features;
    }

    private void buildFeatures(Map<Node, List<Feature>> features, List<String> featureAcs, Node participant) {
        features.put(participant, new ArrayList<>());
        if (participant == null || featureAcs == null) return;

        for (String featureAc : featureAcs) {
            features.get(participant).add(new Feature(network, network.getFeaturesTable().getRow(featureAc)));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge that = (Edge) o;
        return cyEdge.equals(that.cyEdge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cyEdge);
    }

    @Override
    public String toString() {
        return cyEdge.toString();
    }

    @Override
    public boolean isSelected() {
        return edgeRow.get(CyNetwork.SELECTED, Boolean.class);
    }
}
