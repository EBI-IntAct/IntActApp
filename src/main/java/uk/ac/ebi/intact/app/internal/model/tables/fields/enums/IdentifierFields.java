package uk.ac.ebi.intact.app.internal.model.tables.fields.enums;

import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.CVField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;

public class IdentifierFields {
    public static final Field<String> AC = new Field<>(Table.IDENTIFIER, Field.Namespace.IDENTIFIER, "Accession", "xref_ac", String.class);
    public static final Field<String> ID = new Field<>(Table.IDENTIFIER, Field.Namespace.IDENTIFIER, "Identifier", "xref_id", String.class);
    public static final CVField DATABASE = new CVField(Table.IDENTIFIER, Field.Namespace.IDENTIFIER, "Database", "xref_database_name", "xref_database_mi", SourceOntology.MI);
    public static final CVField QUALIFIER = new CVField(Table.IDENTIFIER, Field.Namespace.IDENTIFIER, "Qualifier", "qualifier_name", "qualifier_mi", SourceOntology.MI);
}
