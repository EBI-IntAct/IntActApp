package uk.ac.ebi.intact.intactApp.internal.model.core;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.SourceOntology;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
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
    public final List<String> featureAcs = new ArrayList<>();
    public final List<String> identifierAcs = new ArrayList<>();
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
        this.preferredId = new Identifier(preferredIdDbName, preferredIdDbMIId, preferredId, "preferred id");
        species = nodeRow.get(SPECIES, String.class);
        taxId = nodeRow.get(TAX_ID, Long.class);

        featureAcs.addAll(nodeRow.getList(FEATURES, String.class).stream().filter(s -> !s.isBlank()).collect(toList()));
        identifierAcs.addAll(nodeRow.getList(IDENTIFIERS, String.class).stream().filter(s -> !s.isBlank()).collect(toList()));
    }

    public List<Identifier> getIdentifiers() {
        CyTable identifiersTable = iNetwork.getIdentifiersTable();
        return identifierAcs.stream().map(identifierAc -> new Identifier(identifiersTable.getRow(identifierAc))).collect(Collectors.toList());
    }

    public List<Feature> getFeatures() {
        CyTable featuresTable = iNetwork.getFeaturesTable();
        return featureAcs.stream().map(featureAC -> new Feature(iNetwork, featuresTable.getRow(featureAC))).collect(Collectors.toList());
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
