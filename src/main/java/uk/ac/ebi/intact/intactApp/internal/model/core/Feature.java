package uk.ac.ebi.intact.intactApp.internal.model.core;

import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;

public class Feature {
    public final IntactEvidenceEdge edge;
    public final IntactNode node;
    public final String type;
    public final String typeMIId;
    public final String name;

    public Feature(IntactEvidenceEdge edge, IntactNode node, String type, String typeMIId, String name) {
        this.edge = edge;
        this.node = node;
        this.type = type;
        this.typeMIId = typeMIId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Feature feature = (Feature) o;

        if (!edge.equals(feature.edge)) return false;
        if (!node.equals(feature.node)) return false;
        if (!typeMIId.equals(feature.typeMIId)) return false;
        return name.equals(feature.name);
    }

    @Override
    public int hashCode() {
        int result = edge.hashCode();
        result = 31 * result + node.hashCode();
        result = 31 * result + typeMIId.hashCode();
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
