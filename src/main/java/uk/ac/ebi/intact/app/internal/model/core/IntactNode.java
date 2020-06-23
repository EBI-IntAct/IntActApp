package uk.ac.ebi.intact.app.internal.model.core;

import org.cytoscape.model.*;
import uk.ac.ebi.intact.app.internal.model.IntactNetwork;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.app.internal.model.core.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.core.ontology.SourceOntology;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.*;

public class IntactNode extends Interactor implements Comparable<Interactor>, IntactElement {
    public final IntactNetwork iNetwork;
    public final CyNode node;
    public final Identifier preferredIdentifier;
    public final List<String> featureAcs = new ArrayList<>();
    public final List<String> identifierAcs = new ArrayList<>();

    public IntactNode(final IntactNetwork iNetwork, final CyNode node) {
        this(iNetwork, node, iNetwork.getNetwork().getRow(node));
    }

    public IntactNode(final IntactNetwork iNetwork, final CyNode node, CyRow nodeRow) {
        super(
                nodeRow.get(INTACT_ID, String.class),
                nodeRow.get(CyNetwork.NAME, String.class),
                nodeRow.get(PREFERRED_ID, String.class),
                nodeRow.get(FULL_NAME, String.class),
                nodeRow.get(TYPE, String.class),
                nodeRow.get(SPECIES, String.class),
                nodeRow.get(TAX_ID, Long.class),
                -1
        );
        this.iNetwork = iNetwork;
        this.node = node;
        String preferredIdDbName = nodeRow.get(PREFERRED_ID_DB, String.class);
        OntologyIdentifier preferredIdDbMIId = new OntologyIdentifier(nodeRow.get(PREFERRED_ID_DB_MI_ID, String.class), SourceOntology.MI);
        this.preferredIdentifier = new Identifier(preferredIdDbName, preferredIdDbMIId, preferredId, "preferred id");

        List<String> nodeFeatures = nodeRow.getList(FEATURES, String.class);
        if (nodeFeatures != null) {
            featureAcs.addAll(nodeFeatures.stream().filter(s -> !s.isBlank()).collect(toList()));
        }

        List<String> nodeIdentifiers = nodeRow.getList(IDENTIFIERS, String.class);
        if (nodeIdentifiers != null) {
            identifierAcs.addAll(nodeIdentifiers.stream().filter(s -> !s.isBlank()).collect(toList()));
        }
    }

    public List<IntactEdge> getAdjacentEdges() {
        return iNetwork.getNetwork().getAdjacentEdgeList(node, CyEdge.Type.ANY).stream().map(edge -> IntactEdge.createIntactEdge(iNetwork, edge)).collect(toList());
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

    @Override
    public int compareTo(Interactor o) {
        if (name.isEmpty()) return Integer.MAX_VALUE;
        else if (o.name.isEmpty()) return Integer.MIN_VALUE;
        else return name.compareTo(o.name);
    }


}
