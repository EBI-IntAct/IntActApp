package uk.ac.ebi.intact.app.internal.model.tables.fields.enums;

import org.cytoscape.model.CyNetwork;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.CVField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;

public class NodeFields {
    public static final Field<String> AC = new Field<>(Table.NODE, Field.Namespace.INTACT, "AC", "id", String.class);
    public static final Field<String> NAME = new Field<>(Table.NODE, Field.Namespace.NULL, CyNetwork.NAME, "interactor_name", String.class);
    public static final Field<String> PREFERRED_ID = new Field<>(Table.NODE, Field.Namespace.INTACT, "Preferred Id", "preferred_id", String.class);
    public static final CVField PREFERRED_ID_DB = new CVField(Table.NODE, Field.Namespace.INTACT, "Preferred Id Database", "preferred_id_database_name", "preferred_id_database_mi_identifier", SourceOntology.MI);
    public static final CVField TYPE = new CVField(Table.NODE, Field.Namespace.INTACT, "Type", "type");
    public static final Field<String> SPECIES = new Field<>(Table.NODE, Field.Namespace.INTACT, "Species", "species", String.class);
    public static final Field<String> TAX_ID = new Field<>(Table.NODE, Field.Namespace.INTACT, "Taxon Id", "taxId", String.class);
    public static final Field<String> FULL_NAME = new Field<>(Table.NODE, Field.Namespace.INTACT, "Description", "full_name", String.class);
    public static final Field<Boolean> MUTATED = new Field<>(Table.NODE, Field.Namespace.INTACT, "Mutation", null, Boolean.class,false, Boolean.FALSE);
    public static final ListField<String> FEATURES = new ListField<>(Table.NODE, Field.Namespace.FEATURE, "Features", String.class);
    public static final ListField<String> IDENTIFIERS = new ListField<>(Table.NODE, Field.Namespace.IDENTIFIER, "Identifiers", String.class);
    public static final Field<String> ELABEL_STYLE = new Field<>(Table.NODE, Field.Namespace.NULL, "enhancedLabel Passthrough", null, String.class, "label: attribute=\"name\" labelsize=12 labelAlignment=center outline=true outlineColor=black outlineTransparency=130 outlineWidth=5 background=false color=white dropShadow=false");
}