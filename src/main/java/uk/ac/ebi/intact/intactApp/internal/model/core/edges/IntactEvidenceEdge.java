package uk.ac.ebi.intact.intactApp.internal.model.core.edges;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.*;

public class IntactEvidenceEdge extends IntactEdge {
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

    IntactEvidenceEdge(IntactNetwork iNetwork, CyEdge edge) {
        super(iNetwork, edge);
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

    public Map<IntactNode, List<Feature>> getFeatures() {
        Map<IntactNode, List<Feature>> features = new HashMap<>();
        features.put(source, new ArrayList<>());
        if (target != null)
            features.put(target, new ArrayList<>());

        for (CyRow featureRow : iNetwork.getFeaturesTable().getMatchingRows(ModelUtils.EDGE_REF, edge.getSUID())) {
            String type = featureRow.get(ModelUtils.FEATURE_TYPE, String.class);
            String typeMIId = featureRow.get(ModelUtils.FEATURE_TYPE_MI_ID, String.class);
            String name = featureRow.get(ModelUtils.FEATURE_NAME, String.class);
            if (featureRow.get(ModelUtils.NODE_REF, Long.class).equals(source.node.getSUID())) {
                features.get(source).add(new Feature(this, source, type, typeMIId, name));
            } else {
                features.get(target).add(new Feature(this, target, type, typeMIId, name));
            }
        }
        return features;
    }
}
