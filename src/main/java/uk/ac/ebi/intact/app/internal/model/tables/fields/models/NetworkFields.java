package uk.ac.ebi.intact.app.internal.model.tables.fields.models;

import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.Field;

public class NetworkFields {
    public static final Field<Long> FEATURES_TABLE_REF = new Field<>(Table.NETWORK, Field.Namespace.FEATURE, "Features", null, Long.class);
    public static final Field<Long> IDENTIFIERS_TABLE_REF = new Field<>(Table.NETWORK, Field.Namespace.IDENTIFIER, "Identifiers", null, Long.class);
    public static final Field<String> UUID = new Field<>(Table.NETWORK, Field.Namespace.NULL, "UUID", null, String.class);
    public static final Field<String> VIEW_STATE = new Field<>(Table.NETWORK, Field.Namespace.VIEW, "Data", null, String.class);
}