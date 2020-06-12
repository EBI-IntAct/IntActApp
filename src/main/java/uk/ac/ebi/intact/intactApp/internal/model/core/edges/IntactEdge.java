package uk.ac.ebi.intact.intactApp.internal.model.core.edges;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.*;

import static uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils.SOURCE_FEATURES;
import static uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils.TARGET_FEATURES;

public abstract class IntactEdge {
    public final IntactNetwork iNetwork;
    public final CyEdge edge;
    public final String name;
    public final double miScore;
    public boolean collapsed;
    public final CyRow edgeRow;
    public final IntactNode source;
    public final IntactNode target;
    public final List<String> sourceFeatureAcs;
    public final List<String> targetFeatureAcs;


    public static IntactEdge createIntactEdge(IntactNetwork iNetwork, CyEdge edge) {
        if (iNetwork == null || edge == null) return null;
        CyRow edgeRow = iNetwork.getNetwork().getRow(edge);
        if (edgeRow == null) return null;
        if (edgeRow.get(ModelUtils.C_IS_COLLAPSED, Boolean.class)) {
            return new IntactCollapsedEdge(iNetwork, edge);
        } else {
            return new IntactEvidenceEdge(iNetwork, edge);
        }
    }


    IntactEdge(IntactNetwork iNetwork, CyEdge edge) {
        this.iNetwork = iNetwork;
        this.edge = edge;
        edgeRow = iNetwork.getNetwork().getRow(edge);

        name = edgeRow.get(CyNetwork.NAME, String.class);
        miScore = edgeRow.get(ModelUtils.MI_SCORE, Double.class);
        source = new IntactNode(iNetwork, edge.getSource());
        target = edge.getTarget() != null ? new IntactNode(iNetwork, edge.getTarget()) : null;

        sourceFeatureAcs = edgeRow.getList(SOURCE_FEATURES, String.class);
        if (sourceFeatureAcs != null) {
            sourceFeatureAcs.removeIf(String::isBlank);
        }
        targetFeatureAcs = edgeRow.getList(TARGET_FEATURES, String.class);
        if (targetFeatureAcs != null) {
            targetFeatureAcs.removeIf(String::isBlank);
        }
    }

    public Map<IntactNode, List<Feature>> getFeatures() {
        Map<IntactNode, List<Feature>> features = new HashMap<>();

        buildFeatures(features, sourceFeatureAcs, source);
        buildFeatures(features, targetFeatureAcs, target);
        return features;
    }

    private void buildFeatures(Map<IntactNode, List<Feature>> features, List<String> featureAcs, IntactNode participant) {
        features.put(participant, new ArrayList<>());
        if (participant == null || featureAcs == null) return;

        for (String featureAc : featureAcs) {
            features.get(participant).add(new Feature(iNetwork, iNetwork.getFeaturesTable().getRow(featureAc)));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntactEdge that = (IntactEdge) o;
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
