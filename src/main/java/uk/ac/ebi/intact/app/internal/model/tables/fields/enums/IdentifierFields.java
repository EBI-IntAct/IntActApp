package uk.ac.ebi.intact.app.internal.model.tables.fields.enums;

import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.CVField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.FieldInitializer;

import java.util.ArrayList;
import java.util.List;

public class IdentifierFields {
    public final static List<Field<?>> fields = new ArrayList<>();
    public final static List<FieldInitializer> initializers = new ArrayList<>();

    public static final Field<String> AC = new Field<>(fields, initializers, Field.Namespace.IDENTIFIER, "Accession", "xref_ac", String.class, true);
    public static final Field<String> ID = new Field<>(fields, initializers, Field.Namespace.IDENTIFIER, "Identifier", "xref_id", String.class, true);
    public static final CVField DATABASE = new CVField(fields, initializers, Field.Namespace.IDENTIFIER, "Database", "xref_database_name", "xref_database_mi", SourceOntology.MI, true);
    public static final CVField QUALIFIER = new CVField(fields, initializers, Field.Namespace.IDENTIFIER, "Qualifier", "qualifier_name", "qualifier_mi", SourceOntology.MI, true);
}
