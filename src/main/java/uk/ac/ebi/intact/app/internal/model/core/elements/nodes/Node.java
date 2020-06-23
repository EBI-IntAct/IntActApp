package uk.ac.ebi.intact.app.internal.model.core.elements.nodes;

import org.cytoscape.model.*;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.Identifier;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.*;

public class Node extends Interactor implements Comparable<Interactor>, Element {
    public final Network network;
    public final CyNode node;
    public final Identifier preferredIdentifier;
    public final List<String> featureAcs = new ArrayList<>();
    public final List<String> identifierAcs = new ArrayList<>();

    public Node(final Network network, final CyNode node) {
        this(network, node, network.getCyNetwork().getRow(node));
    }

    public Node(final Network network, final CyNode node, CyRow nodeRow) {
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
        this.network = network;
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

    public List<Edge> getAdjacentEdges() {
        return network.getCyNetwork().getAdjacentEdgeList(node, CyEdge.Type.ANY).stream().map(edge -> Edge.createIntactEdge(network, edge)).collect(toList());
    }

    public List<Identifier> getIdentifiers() {
        CyTable identifiersTable = network.getIdentifiersTable();
        return identifierAcs.stream().map(identifierAc -> new Identifier(identifiersTable.getRow(identifierAc))).collect(Collectors.toList());
    }

    public List<Feature> getFeatures() {
        CyTable featuresTable = network.getFeaturesTable();
        return featureAcs.stream().map(featureAC -> new Feature(network, featuresTable.getRow(featureAC))).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node that = (Node) o;
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
