package uk.ac.ebi.intact.intactApp.internal.model.core;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.Objects;

public class Identifier {
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
        this.databaseName = identifierRow.get(ModelUtils.IDENTIFIER_DB_NAME, String.class);
        this.databaseIdentifier = new OntologyIdentifier(identifierRow.get(ModelUtils.IDENTIFIER_DB_MI_ID, String.class));
        this.id = identifierRow.get(ModelUtils.IDENTIFIER_ID, String.class);
        this.qualifier = identifierRow.get(ModelUtils.IDENTIFIER_QUALIFIER, String.class);
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
}

