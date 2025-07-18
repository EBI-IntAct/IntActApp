package uk.ac.ebi.intact.app.internal.model.tables.fields.enums;

import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.FieldInitializer;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field.Namespace.NULL;

public class StyleSettingsFields {
    public final static List<Field<?>> fields = new ArrayList<>();
    public final static List<FieldInitializer> initializers = new ArrayList<>();

    public static final Field<String> TYPE =  new Field<>(fields, initializers, NULL, "type", null, String.class, true);
    public static final Field<String> JSON_VALUE =  new Field<>(fields, initializers, NULL, "JSON value", null, String.class, true);
}
