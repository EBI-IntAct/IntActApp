package uk.ac.ebi.intact.app.internal.model.tables.fields.models;

import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.Field;

public class IdentifierFields {
    // Identifier table columns
    public static final Field<String> AC = new Field<>(Table.IDENTIFIER, Field.Namespace.IDENTIFIER, "Accession", "xref_ac", String.class);
    public static final Field<String> ID = new Field<>(Table.IDENTIFIER, Field.Namespace.IDENTIFIER, "Identifier", "xref_id", String.class);
    public static final Field<String> DB_NAME = new Field<>(Table.IDENTIFIER, Field.Namespace.IDENTIFIER, "Database Name", "xref_database_name", String.class);
    public static final Field<String> DB_MI_ID = new Field<>(Table.IDENTIFIER, Field.Namespace.IDENTIFIER, "Database MI Id", "xref_database_name", String.class);
    public static final Field<String> QUALIFIER = new Field<>(Table.IDENTIFIER, Field.Namespace.IDENTIFIER, "Qualifier", "qualifier_name", String.class);
    public static final Field<String> QUALIFIER_ID = new Field<>(Table.IDENTIFIER, Field.Namespace.IDENTIFIER, "Qualifier MI Id", "qualifier_mi", String.class);
}
