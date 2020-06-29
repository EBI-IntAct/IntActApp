package uk.ac.ebi.intact.app.internal.utils.tables.fields.models;

import uk.ac.ebi.intact.app.internal.utils.tables.Table;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.Field;

public class NetworkFields {
    public static final Field<Long> FEATURES_TABLE_REF = new Field<>(Table.NETWORK, Field.Namespace.NULL, "Features.SUID", null, Long.class);
    public static final Field<Long> IDENTIFIERS_TABLE_REF = new Field<>(Table.NETWORK, Field.Namespace.NULL, "Identifiers.SUID", null, Long.class);
    public static final Field<String> UUID = new Field<>(Table.NETWORK, Field.Namespace.NULL, "UUID", null, String.class);
    public static final Field<String> VIEW_STATE = new Field<>(Table.NETWORK, Field.Namespace.VIEW, "Data", null, String.class);
}