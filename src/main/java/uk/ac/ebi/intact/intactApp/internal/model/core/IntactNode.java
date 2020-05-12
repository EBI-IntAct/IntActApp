package uk.ac.ebi.intact.intactApp.internal.model.core;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IntactNode {
    public final IntactNetwork iNetwork;
    public final CyNode node;
    public final String id;
    public final String name;
    public final String type;
    public final String preferredId;
    public final String preferredIdDb;
    public final String species;
    public final long taxId;

    public IntactNode(final IntactNetwork iNetwork, final CyNode node) {
        this.iNetwork = iNetwork;
        this.node = node;
        CyRow nodeRow = iNetwork.getNetwork().getRow(node);
        name = nodeRow.get(CyNetwork.NAME, String.class);
        id = nodeRow.get(ModelUtils.INTACT_ID, String.class);
        type = nodeRow.get(ModelUtils.TYPE, String.class);
        preferredId = nodeRow.get(ModelUtils.PREFERRED_ID, String.class);
        preferredIdDb = nodeRow.get(ModelUtils.PREFERRED_ID_DB, String.class);
        species = nodeRow.get(ModelUtils.SPECIES, String.class);
        taxId = nodeRow.get(ModelUtils.TAX_ID, Long.class);
    }

    public List<Identifier> getIdentifiers() {
        List<Identifier> identifiers = new ArrayList<>();
        for (CyRow idRow : iNetwork.getIdentifiersTable().getMatchingRows(ModelUtils.NODE_REF, node.getSUID())) {
            identifiers.add(new Identifier(this, idRow));
        }
        return identifiers;
    }

    public List<Feature> getFeatures() {
        List<Feature> features = new ArrayList<>();
        for (CyRow featureRow : iNetwork.getFeaturesTable().getMatchingRows(ModelUtils.NODE_REF, node.getSUID())) {
            String type = featureRow.get(ModelUtils.FEATURE_TYPE, String.class);
            String typeMIId = featureRow.get(ModelUtils.FEATURE_TYPE_MI_ID, String.class);
            String name = featureRow.get(ModelUtils.FEATURE_NAME, String.class);
            IntactEvidenceEdge edge = (IntactEvidenceEdge) IntactEdge.createIntactEdge(iNetwork, iNetwork.getNetwork().getEdge(featureRow.get(ModelUtils.EDGE_REF, Long.class)));
            if (featureRow.get(ModelUtils.NODE_REF, Long.class).equals(node.getSUID())) {
                features.add(new Feature(edge, this, type, typeMIId, name));
            }
        }
        return features;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntactNode that = (IntactNode) o;
        return node.equals(that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }

    @Override
    public String toString() {
        return node.toString();
    }
}
