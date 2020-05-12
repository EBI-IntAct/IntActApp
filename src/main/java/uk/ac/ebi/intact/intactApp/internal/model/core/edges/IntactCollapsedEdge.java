package uk.ac.ebi.intact.intactApp.internal.model.core.edges;

import org.cytoscape.model.*;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.*;

public class IntactCollapsedEdge extends IntactEdge {
    public final Map<Long, IntactEvidenceEdge> edges = new HashMap<>();

    IntactCollapsedEdge(IntactNetwork iNetwork, CyEdge edge) {
        super(iNetwork, edge);
        List<Long> edgeIds = edgeRow.getList(ModelUtils.C_INTACT_IDS, Long.class);
        CyNetwork network = iNetwork.getNetwork();
        CyTable edgeTable = network.getDefaultEdgeTable();
        for (Long edgeId : edgeIds) {
            Long edgeSUID = edgeTable
                    .getMatchingRows(ModelUtils.INTACT_ID, edgeId)
                    .iterator().next()
                    .get(CyIdentifiable.SUID, Long.class);
            CyEdge cyEdge = network.getEdge(edgeSUID);
            edges.put(edgeSUID, new IntactEvidenceEdge(iNetwork, cyEdge));
        }
    }

    public Map<IntactNode, List<Feature>> getFeatures() {
        Map<IntactNode, List<Feature>> features = new HashMap<>();

        for (IntactNode node : List.of(source, target)) {
            features.put(node, new ArrayList<>());

            for (CyRow featureRow : iNetwork.getFeaturesTable().getMatchingRows(ModelUtils.NODE_REF, node.node.getSUID())) {
                Long edgeSUID = featureRow.get(ModelUtils.EDGE_REF, Long.class);
                if (edges.containsKey(edgeSUID)) {
                    String type = featureRow.get(ModelUtils.FEATURE_TYPE, String.class);
                    String typeMIId = featureRow.get(ModelUtils.FEATURE_TYPE_MI_ID, String.class);
                    String name = featureRow.get(ModelUtils.FEATURE_NAME, String.class);
                    features.get(node).add(new Feature(edges.get(edgeSUID), node, type, typeMIId, name));
                }
            }
        }
        return features;
    }
}
