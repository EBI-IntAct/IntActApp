package uk.ac.ebi.intact.intactApp.internal.model.core;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

public class Identifier {
    public final IntactNode node;
    public final String databaseName;
    public final String databaseIdentifier;
    public final String id;

    public Identifier(IntactNode node, String databaseName, String databaseIdentifier, String id) {
        this.node = node;
        this.databaseName = databaseName;
        this.databaseIdentifier = databaseIdentifier;
        this.id = id;
    }

    public Identifier(IntactNode node, CyRow identifierRow) {
        this.node = node;
        this.databaseName = identifierRow.get(ModelUtils.IDENTIFIER_DB_NAME, String.class);
        this.databaseIdentifier = identifierRow.get(ModelUtils.IDENTIFIER_DB_MI_ID, String.class);
        this.id = identifierRow.get(ModelUtils.IDENTIFIER_ID, String.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identifier that = (Identifier) o;

        if (!node.equals(that.node)) return false;
        if (!databaseName.equals(that.databaseName)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = node.hashCode();
        result = 31 * result + databaseName.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
