package uk.ac.ebi.intact.app.internal.model.core.elements.nodes;

import org.cytoscape.model.*;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.Identifier;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NodeFields.*;

public class Node extends Interactor implements Comparable<Interactor>, Element {
    public final Network network;
    public final CyNode cyNode;
    public final CVTerm type;
    public final Identifier preferredIdentifier;
    public final List<String> featureAcs = new ArrayList<>();
    public final List<String> identifierAcs = new ArrayList<>();
    public final CyRow nodeRow;
    public boolean mutated;

    public Node(final Network network, final CyNode cyNode) {
        this(network, cyNode, network.getCyNetwork().getRow(cyNode));
    }

    private Node(final Network network, final CyNode cyNode, CyRow nodeRow) {
        super(
                AC.getValue(nodeRow),
                NAME.getValue(nodeRow),
                PREFERRED_ID.getValue(nodeRow),
                FULL_NAME.getValue(nodeRow),
                TYPE.VALUE.getValue(nodeRow),
                SPECIES.getValue(nodeRow),
                TAX_ID.getValue(nodeRow),
                -1
        );
        this.network = network;
        this.cyNode = cyNode;
        this.type =  new CVTerm(nodeRow, TYPE);
        this.nodeRow = nodeRow;
        this.mutated = MUTATED.getValue(nodeRow);
        String preferredIdDbName = PREFERRED_ID_DB.VALUE.getValue(nodeRow);
        OntologyIdentifier preferredIdDbMIId = new OntologyIdentifier(PREFERRED_ID_DB.ID.getValue(nodeRow), SourceOntology.MI);
        this.preferredIdentifier = new Identifier(preferredIdDbName, preferredIdDbMIId, preferredId, "preferred id");

        List<String> nodeFeatures = FEATURES.getValue(nodeRow);
        if (nodeFeatures != null) {
            featureAcs.addAll(nodeFeatures.stream().filter(s -> !s.isBlank()).collect(toList()));
        }

        List<String> nodeIdentifiers = IDENTIFIERS.getValue(nodeRow);
        if (nodeIdentifiers != null) {
            identifierAcs.addAll(nodeIdentifiers.stream().filter(s -> !s.isBlank()).collect(toList()));
        }
    }

    public List<Edge> getAdjacentEdges() {
        return network.getCyNetwork().getAdjacentEdgeList(cyNode, CyEdge.Type.ANY).stream().map(edge -> Edge.createEdge(network, edge)).collect(toList());
    }

    public List<Identifier> getIdentifiers() {
        CyTable identifiersTable = network.getIdentifiersTable();
        if (identifiersTable == null) return new ArrayList<>();
        return identifierAcs.stream().map(identifierAc -> new Identifier(identifiersTable.getRow(identifierAc))).collect(Collectors.toList());
    }

    public List<Feature> getFeatures() {
        CyTable featuresTable = network.getFeaturesTable();
        if (featuresTable == null) return new ArrayList<>();
        Set<Long> adjacentEdgesSUID = network.getCyNetwork().getAdjacentEdgeList(cyNode, CyEdge.Type.ANY).stream().map(CyIdentifiable::getSUID).collect(Collectors.toSet());
        return featureAcs.stream()
                .map(featureAC -> new Feature(network, featuresTable.getRow(featureAC)))
                .filter(feature -> feature.isPresentIn(adjacentEdgesSUID))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node that = (Node) o;
        return cyNode.equals(that.cyNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cyNode);
    }

    @Override
    public String toString() {
        return cyNode.toString();
    }

    @Override
    public int compareTo(Interactor o) {
        if (name.isEmpty()) return Integer.MAX_VALUE;
        else if (o.name.isEmpty()) return Integer.MIN_VALUE;
        else return name.compareTo(o.name);
    }


    @Override
    public boolean isSelected() {
        return nodeRow.get(CyNetwork.SELECTED, Boolean.class);
    }

    public void updateMutationStatus() {
        mutated = getFeatures().stream().anyMatch(feature -> FeatureClassifier.mutation.contains(feature.type.id));
        MUTATED.setValue(nodeRow, mutated);
    }
}
