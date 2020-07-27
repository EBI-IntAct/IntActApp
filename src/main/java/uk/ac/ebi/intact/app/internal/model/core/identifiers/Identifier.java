package uk.ac.ebi.intact.app.internal.model.core.identifiers;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.CVField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;

import java.util.Objects;

import static uk.ac.ebi.intact.app.internal.model.tables.fields.enums.IdentifierFields.*;

public class Identifier implements Comparable<Identifier> {
    public final String id;
    public final CVTerm database;
    public final String qualifier;

    public Identifier(String databaseName, OntologyIdentifier databaseIdentifier, String id, String qualifier) {
        this.id = id;
        this.database = new CVTerm(databaseName, databaseIdentifier);
        this.qualifier = qualifier;
    }

    public Identifier(CyRow identifierRow) {
        this(identifierRow, ID, DATABASE, QUALIFIER);
    }

    public Identifier(CyRow row, Field<String> idField, CVField databaseField, CVField qualifierField) {
        this.id = idField.getValue(row);
        this.database = new CVTerm(row, databaseField);
        this.qualifier = qualifierField.VALUE.getValue(row);
//        this.qualifier = new CVTerm(row, qualifierField);
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
        return id + " from " + database.value;
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

