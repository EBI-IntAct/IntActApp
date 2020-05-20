package uk.ac.ebi.intact.intactApp.internal.model.core;

import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.OntologyIdentifier;

public class Feature {
    public final IntactEvidenceEdge edge;
    public final IntactNode node;
    public final String type;
    public final OntologyIdentifier typeIdentifier;
    public final String name;

    public Feature(IntactEvidenceEdge edge, IntactNode node, String type, OntologyIdentifier typeIdentifier, String name) {
        this.edge = edge;
        this.node = node;
        this.type = type;
        this.typeIdentifier = typeIdentifier;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Feature feature = (Feature) o;

        if (!edge.equals(feature.edge)) return false;
        if (!node.equals(feature.node)) return false;
        if (!typeIdentifier.equals(feature.typeIdentifier)) return false;
        return name.equals(feature.name);
    }

    @Override
    public int hashCode() {
        int result = edge.hashCode();
        result = 31 * result + node.hashCode();
        result = 31 * result + typeIdentifier.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "edge=" + edge +
                ", node=" + node +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
