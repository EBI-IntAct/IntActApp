package uk.ac.ebi.intact.app.internal.utils.tables.fields.models;

import org.cytoscape.model.CyNetwork;
import uk.ac.ebi.intact.app.internal.utils.tables.Table;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.Field;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.ListField;

public class NodeFields {
    public static final Field<String> AC = new Field<>(Table.NODE, Field.Namespace.INTACT, "AC", "id", String.class);
    public static final Field<String> NAME = new Field<>(Table.NODE, Field.Namespace.NULL, CyNetwork.NAME, "interactor_name", String.class);
    public static final Field<String> PREFERRED_ID = new Field<>(Table.NODE, Field.Namespace.INTACT, "Preferred Id", "preferred_id", String.class);
    public static final Field<String> PREFERRED_ID_DB = new Field<>(Table.NODE, Field.Namespace.INTACT, "Preferred Id Database", "preferred_id_database_name", String.class);
    public static final Field<String> PREFERRED_ID_DB_MI_ID = new Field<>(Table.NODE, Field.Namespace.INTACT, "Preferred Id Database MI Id", "preferred_id_database_mi_identifier", String.class);
    public static final Field<String> TYPE = new Field<>(Table.NODE, Field.Namespace.INTACT, "Type", "type", String.class);
    public static final Field<String> TYPE_MI_ID = new Field<>(Table.NODE, Field.Namespace.INTACT, "Type MI Id", "type_mi_identifier", String.class);
    public static final Field<String> SPECIES = new Field<>(Table.NODE, Field.Namespace.INTACT, "Species", "species", String.class);
    public static final Field<Long> TAX_ID = new Field<>(Table.NODE, Field.Namespace.INTACT, "Taxon Id", "taxId", Long.class);
    public static final Field<String> FULL_NAME = new Field<>(Table.NODE, Field.Namespace.INTACT, "Description", "full_name", String.class);
    public static final Field<Boolean> MUTATED = new Field<>(Table.NODE, Field.Namespace.INTACT, "Mutation", null, Boolean.class, false);
    public static final ListField<String> FEATURES = new ListField<>(Table.NODE, Field.Namespace.FEATURE, "Features", String.class);
    public static final ListField<String> IDENTIFIERS = new ListField<>(Table.NODE, Field.Namespace.IDENTIFIER, "Identifiers", String.class);
    public static final Field<String> ELABEL_STYLE = new Field<>(Table.NODE, Field.Namespace.NULL, "enhancedLabel Passthrough", null, String.class, "label: attribute=\"name\" labelsize=12 labelAlignment=center outline=true outlineColor=black outlineTransparency=130 outlineWidth=5 background=false color=white dropShadow=false");
}