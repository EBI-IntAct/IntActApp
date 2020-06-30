package uk.ac.ebi.intact.app.internal.model.core.identifiers;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.IdentifierFields;

import java.util.Objects;

public class Identifier implements Comparable<Identifier> {
    public final String databaseName;
    public final OntologyIdentifier databaseIdentifier;
    public final String id;
    public final String qualifier;

    public Identifier(String databaseName, OntologyIdentifier databaseIdentifier, String id, String qualifier) {
        this.databaseName = databaseName;
        this.databaseIdentifier = databaseIdentifier;
        this.id = id;
        this.qualifier = qualifier;
    }

    public Identifier(CyRow identifierRow) {
        this.databaseName = IdentifierFields.DB_NAME.getValue(identifierRow);

        this.databaseIdentifier = new OntologyIdentifier(IdentifierFields.DB_MI_ID.getValue(identifierRow));
        this.id = IdentifierFields.ID.getValue(identifierRow);
        this.qualifier = IdentifierFields.QUALIFIER.getValue(identifierRow);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " from " + databaseName;
    }

    @Override
    public int compareTo(Identifier o) {
        int score = 0;
        if (qualifier != null && qualifier.equals("identity")) score--;
        if (o.qualifier != null && o.qualifier.equals("identity")) score++;
        if (score == 0) return id.compareTo(o.id);
        return score;
    }
}

