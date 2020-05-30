package uk.ac.ebi.intact.intactApp.internal.model.core;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.intactApp.internal.utils.TableUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils.*;

public class Feature {
    public final IntactNetwork network;
    public final String ac;
    public final String type;
    public final OntologyIdentifier typeIdentifier;
    public final String name;
    public final List<Long> edgeIDs = new ArrayList<>();
    public final List<Long> edgeSUIDs = new ArrayList<>();

    public Feature(IntactNetwork network, CyRow featureRow) {
        this.network = network;
        this.ac = featureRow.get(FEATURE_AC, String.class);
        this.name = featureRow.get(FEATURE_NAME, String.class);
        this.type = featureRow.get(FEATURE_TYPE, String.class);
        this.typeIdentifier = TableUtil.getOntologyIdentifier(featureRow, FEATURE_TYPE_MI_ID, FEATURE_TYPE_MOD_ID, FEATURE_TYPE_PAR_ID);
        edgeIDs.addAll(featureRow.getList(FEATURE_EDGE_IDS, Long.class));
        edgeSUIDs.addAll(featureRow.getList(FEATURE_EDGE_SUIDS, Long.class));
    }

    public List<IntactEvidenceEdge> getEdges() {
        return edgeSUIDs.stream()
                .map(suid -> (IntactEvidenceEdge) IntactEdge.createIntactEdge(network, network.getNetwork().getEdge(suid)))
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
