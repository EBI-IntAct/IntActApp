package uk.ac.ebi.intact.app.internal.model.tables.fields.models;

import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.ListField;

public class FeatureFields {
    public static final Field<String> AC = new Field<>(Table.FEATURE, Field.Namespace.FEATURE, "Accession", "feature_ac", String.class);
    public static final Field<String> NAME = new Field<>(Table.FEATURE, Field.Namespace.FEATURE, "Name", "feature_name", String.class);
    public static final Field<String> TYPE = new Field<>(Table.FEATURE, Field.Namespace.FEATURE, "Type", "feature_type", String.class);
    public static final Field<String> TYPE_MI_ID = new Field<>(Table.FEATURE, Field.Namespace.FEATURE, "Type MI Id", "feature_type_mi_identifier", String.class);
    public static final Field<String> TYPE_MOD_ID = new Field<>(Table.FEATURE, Field.Namespace.FEATURE, "Type Mod Id", "feature_type_mod_identifier", String.class);
    public static final Field<String> TYPE_PAR_ID = new Field<>(Table.FEATURE, Field.Namespace.FEATURE, "Type Par Id", "feature_type_par_identifier", String.class);
    public static final ListField<Long> EDGES_SUID = new ListField<>(Table.FEATURE, Field.Namespace.FEATURE, "Edge SUIDs", Long.class);
}
