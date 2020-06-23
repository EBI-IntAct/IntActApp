package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;

import java.util.*;

import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.SOURCE_FEATURES;
import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.TARGET_FEATURES;

public abstract class Edge implements Element {
    public final Network network;
    public final CyEdge edge;
    public final String name;
    public final double miScore;
    public boolean collapsed;
    public final CyRow edgeRow;
    public final Node source;
    public final Node target;
    public final List<String> sourceFeatureAcs;
    public final List<String> targetFeatureAcs;


    public static Edge createIntactEdge(Network network, CyEdge edge) {
        if (network == null || edge == null) return null;
        CyRow edgeRow = network.getCyNetwork().getRow(edge);
        if (edgeRow == null) return null;
        Boolean isCollapsed = edgeRow.get(ModelUtils.C_IS_COLLAPSED, Boolean.class);
        if (isCollapsed == null) return null;
        if (isCollapsed) {
            return new CollapsedEdge(network, edge);
        } else {
            return new EvidenceEdge(network, edge);
        }
    }


    Edge(Network network, CyEdge edge) {
        this.network = network;
        this.edge = edge;
        edgeRow = network.getCyNetwork().getRow(edge);

        name = edgeRow.get(CyNetwork.NAME, String.class);
        miScore = edgeRow.get(ModelUtils.MI_SCORE, Double.class);
        source = new Node(network, edge.getSource());
        target = edge.getTarget() != null ? new Node(network, edge.getTarget()) : null;

        sourceFeatureAcs = edgeRow.getList(SOURCE_FEATURES, String.class);
        if (sourceFeatureAcs != null) {
            sourceFeatureAcs.removeIf(String::isBlank);
        }
        targetFeatureAcs = edgeRow.getList(TARGET_FEATURES, String.class);
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
        return edge.equals(that.edge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edge);
    }

    @Override
    public String toString() {
        return edge.toString();
    }
}
