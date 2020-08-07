package uk.ac.ebi.intact.app.internal.model.tables.fields.enums;

import org.cytoscape.model.CyNetwork;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.FieldInitializer;

import java.util.ArrayList;
import java.util.List;

public class NetworkFields {
    public final static List<Field<?>> fields = new ArrayList<>();
    public final static List<FieldInitializer> initializers = new ArrayList<>();

    public static final Field<Long> SUID = new Field<>(fields, initializers, Field.Namespace.NULL, CyNetwork.SUID, null, Long.class);
    public static final Field<Long> FEATURES_TABLE_REF = new Field<>(fields, initializers, Field.Namespace.FEATURE, "Features", null, Long.class);
    public static final Field<Long> IDENTIFIERS_TABLE_REF = new Field<>(fields, initializers, Field.Namespace.IDENTIFIER, "Identifiers", null, Long.class);
    public static final Field<String> UUID = new Field<>(fields, initializers, Field.Namespace.NULL, "UUID", null, String.class);
    public static final Field<String> VIEW_STATE = new Field<>(fields, initializers, Field.Namespace.VIEW, "Data", null, String.class);
}