package uk.ac.ebi.intact.app.internal.model.core.features;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.FeatureFields;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Feature {
    public final Network network;
    public final String ac;
    public final CVTerm type;
    public final String name;
    public final List<Long> edgeSUIDs = new ArrayList<>();

    public Feature(Network network, CyRow featureRow) {
        this.network = network;
        this.ac = FeatureFields.AC.getValue(featureRow);
        this.name = FeatureFields.NAME.getValue(featureRow);
        this.type = new CVTerm(featureRow, FeatureFields.TYPE, FeatureFields.TYPE_MI_ID, FeatureFields.TYPE_MOD_ID, FeatureFields.TYPE_PAR_ID);
        this.edgeSUIDs.addAll(FeatureFields.EDGES_SUID.getValue(featureRow));
    }

    public List<EvidenceEdge> getEdges() {
        return edgeSUIDs.stream()
                .map(suid -> (EvidenceEdge) Edge.createEdge(network, network.getCyNetwork().getEdge(suid)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean isPresent() {
        return edgeSUIDs.stream().anyMatch(edgeSUID -> network.getCyNetwork().getEdge(edgeSUID) != null);
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
