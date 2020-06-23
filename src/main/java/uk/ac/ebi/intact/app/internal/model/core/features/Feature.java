package uk.ac.ebi.intact.app.internal.model.core.features;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.*;

public class Feature {
    public final Network network;
    public final String ac;
    public final String type;
    public final OntologyIdentifier typeIdentifier;
    public final String name;
    public final List<Long> edgeIDs = new ArrayList<>();
    public final List<Long> edgeSUIDs = new ArrayList<>();

    public Feature(Network network, CyRow featureRow) {
        this.network = network;
        this.ac = featureRow.get(FEATURE_AC, String.class);
        this.name = featureRow.get(FEATURE_NAME, String.class);
        this.type = featureRow.get(FEATURE_TYPE, String.class);
        this.typeIdentifier = TableUtil.getOntologyIdentifier(featureRow, FEATURE_TYPE_MI_ID, FEATURE_TYPE_MOD_ID, FEATURE_TYPE_PAR_ID);
        edgeIDs.addAll(featureRow.getList(FEATURE_EDGE_IDS, Long.class));
        edgeSUIDs.addAll(featureRow.getList(FEATURE_EDGE_SUIDS, Long.class));
    }

    public List<EvidenceEdge> getEdges() {
        return edgeSUIDs.stream()
                .map(suid -> (EvidenceEdge) Edge.createIntactEdge(network, network.getCyNetwork().getEdge(suid)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feature feature = (Feature) o;
        return Objects.equals(ac, feature.ac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ac);
    }

    @Override
    public String toString() {
        return "Feature{" +
                "ac='" + ac + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}