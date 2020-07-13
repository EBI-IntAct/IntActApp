package uk.ac.ebi.intact.app.internal.model.core.features;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.FeatureFields;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Feature {
    public final Network network;
    public final CyRow featureRow;
    public final String ac;
    public final CVTerm type;
    public final String name;

    public Feature(Network network, CyRow featureRow) {
        this.network = network;
        this.featureRow = featureRow;
        this.ac = FeatureFields.AC.getValue(featureRow);
        this.name = FeatureFields.NAME.getValue(featureRow);
        this.type = new CVTerm(featureRow, FeatureFields.TYPE, FeatureFields.TYPE_MI_ID, FeatureFields.TYPE_MOD_ID, FeatureFields.TYPE_PAR_ID);
    }

    public List<EvidenceEdge> getEdges() {
        return FeatureFields.EDGES_SUID.getValue(featureRow).stream()
                .map(suid -> (EvidenceEdge) Edge.createEdge(network, network.getCyNetwork().getEdge(suid)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean isPresent() {
        return FeatureFields.EDGES_SUID.getValue(featureRow).stream()
                .anyMatch(edgeSUID -> network.getCyNetwork().getEdge(edgeSUID) != null);
    }

    public boolean isPresentIn(Set<Long> edgesSUID) {
        return FeatureFields.EDGES_SUID.getValue(featureRow).stream()
                .anyMatch(edgesSUID::contains);
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
