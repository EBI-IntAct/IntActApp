package uk.ac.ebi.intact.app.internal.model.tables.fields.enums;

import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.FieldInitializer;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;

import java.util.ArrayList;
import java.util.List;

public class FeatureFields {
    public final static List<Field<?>> fields = new ArrayList<>();
    public final static List<FieldInitializer> initializers = new ArrayList<>();

    public static final Field<String> AC = new Field<>(fields, initializers, Field.Namespace.FEATURE, "Accession", "feature_ac", String.class);
    public static final Field<String> NAME = new Field<>(fields, initializers, Field.Namespace.FEATURE, "Name", "feature_name", String.class);
    public static final Field<String> TYPE = new Field<>(fields, initializers, Field.Namespace.FEATURE, "Type", "feature_type", String.class);
    public static final Field<String> TYPE_MI_ID = new Field<>(fields, initializers, Field.Namespace.FEATURE, "Type MI Id", "feature_type_mi_identifier", String.class);
    public static final Field<String> TYPE_MOD_ID = new Field<>(fields, initializers, Field.Namespace.FEATURE, "Type Mod Id", "feature_type_mod_identifier", String.class);
    public static final Field<String> TYPE_PAR_ID = new Field<>(fields, initializers, Field.Namespace.FEATURE, "Type Par Id", "feature_type_par_identifier", String.class);
    public static final ListField<Long> EDGES_SUID = new ListField<>(fields, initializers, Field.Namespace.FEATURE, "Edge SUIDs", Long.class);
}
