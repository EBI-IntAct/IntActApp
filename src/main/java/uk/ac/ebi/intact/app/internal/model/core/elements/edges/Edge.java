package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields;

import java.lang.ref.WeakReference;
import java.util.*;

import static uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields.*;

public abstract class Edge implements Element {
    private final WeakReference<Network> network;
    public final CyEdge cyEdge;
    public final String name;
    public final CyRow edgeRow;
    public final Node source;
    public final Node target;
    public final Double miScore;
    public final List<String> sourceFeatureAcs;
    public final List<String> targetFeatureAcs;
    public final boolean isNegative;

    public static Edge createEdge(Network network, CyEdge edge) {
        if (network == null || edge == null) return null;
        CyRow edgeRow = network.getCyNetwork().getRow(edge);
        if (edgeRow == null) return null;
        Boolean isSummary = EdgeFields.IS_SUMMARY.getValue(edgeRow);
        if (isSummary) {
            return new SummaryEdge(network, edge);
        } else {
            return new EvidenceEdge(network, edge);
        }
    }

    Edge(Network network, CyEdge cyEdge) {
        this.network = new WeakReference<>(network);
        this.cyEdge = cyEdge;
        edgeRow = network.getCyNetwork().getRow(cyEdge);

        name = edgeRow.get(CyNetwork.NAME, String.class);
        miScore = EdgeFields.MI_SCORE.getValue(edgeRow);
        source = network.getNode(cyEdge.getSource());
        target = cyEdge.getTarget() != null ? network.getNode(cyEdge.getTarget()) : null;

        sourceFeatureAcs = FEATURES.SOURCE.getValue(edgeRow);
        if (sourceFeatureAcs != null) {
            sourceFeatureAcs.removeIf(String::isBlank);
        }
        targetFeatureAcs = FEATURES.TARGET.getValue(edgeRow);
        if (targetFeatureAcs != null) {
            targetFeatureAcs.removeIf(String::isBlank);
        }

        isNegative = IS_NEGATIVE_INTERACTION.getValue(edgeRow);
    }

    public abstract Map<Node, List<Feature>> getFeatures();

    public abstract boolean isNegative();

    public abstract boolean isSpokeExpansion();

    public abstract Collection<String> getHostOrganisms();

    public abstract Collection<String> getInteractionDetectionMethods();

    public abstract Collection<String> getParticipantDetectionMethods();

    public abstract Collection<String> getTypes();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge that = (Edge) o;
        return cyEdge.equals(that.cyEdge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cyEdge);
    }

    @Override
    public String toString() {
        return cyEdge.toString();
    }

    @Override
    public boolean isSelected() {
        return edgeRow.get(CyNetwork.SELECTED, Boolean.class);
    }

    public Network getNetwork() {
        return Objects.requireNonNull(network.get());
    }
}
