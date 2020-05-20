package uk.ac.ebi.intact.intactApp.internal.model.core;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.SourceOntology;
import uk.ac.ebi.intact.intactApp.internal.utils.TableUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils.*;

public class IntactNode {
    public final IntactNetwork iNetwork;
    public final CyNode node;
    public final String id;
    public final String name;
    public final String fullName;
    public final String type;
    public final Identifier preferredId;
    public final String species;
    public final long taxId;

    public IntactNode(final IntactNetwork iNetwork, final CyNode node) {
        this.iNetwork = iNetwork;
        this.node = node;
        CyRow nodeRow = iNetwork.getNetwork().getRow(node);
        name = nodeRow.get(CyNetwork.NAME, String.class);
        fullName = nodeRow.get(FULL_NAME, String.class);
        id = nodeRow.get(INTACT_ID, String.class);
        type = nodeRow.get(TYPE, String.class);
        String preferredIdDbName = nodeRow.get(PREFERRED_ID_DB, String.class);
        OntologyIdentifier preferredIdDbMIId = new OntologyIdentifier(nodeRow.get(PREFERRED_ID_DB_MI_ID, String.class), SourceOntology.MI);
        String preferredId = nodeRow.get(PREFERRED_ID, String.class);
        this.preferredId = new Identifier(this, preferredIdDbName, preferredIdDbMIId, preferredId, "preferred id");
        species = nodeRow.get(SPECIES, String.class);
        taxId = nodeRow.get(TAX_ID, Long.class);
    }

    public List<Identifier> getIdentifiers() {
        List<Identifier> identifiers = new ArrayList<>();
        for (CyRow idRow : iNetwork.getIdentifiersTable().getMatchingRows(NODE_REF, node.getSUID())) {
            identifiers.add(new Identifier(this, idRow));
        }
        return identifiers;
    }

    public List<Feature> getFeatures() {
        List<Feature> features = new ArrayList<>();
        for (CyRow featureRow : iNetwork.getFeaturesTable().getMatchingRows(NODE_REF, node.getSUID())) {
            String type = featureRow.get(FEATURE_TYPE, String.class);
            OntologyIdentifier typeId = TableUtil.getOntologyIdentifier(featureRow, FEATURE_TYPE_MI_ID, FEATURE_TYPE_MOD_ID, FEATURE_TYPE_PAR_ID);
            String name = featureRow.get(FEATURE_NAME, String.class);
            IntactEvidenceEdge edge = (IntactEvidenceEdge) IntactEdge.createIntactEdge(iNetwork, iNetwork.getNetwork().getEdge(featureRow.get(EDGE_REF, Long.class)));
            if (featureRow.get(NODE_REF, Long.class).equals(node.getSUID())) {
                features.add(new Feature(edge, this, type, typeId, name));
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
