package uk.ac.ebi.intact.app.internal.model.tables.fields.model.participant;

import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.FieldInitializer;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;

import java.util.List;

public class ParticipantListField<E> {
    public final ListField<E> SOURCE;
    public final ListField<E> TARGET;

    public ParticipantListField( List<Field<?>> fields, List<FieldInitializer> initializers, String name, Class<E> type) {
        this(fields, initializers, name, type, false);
    }

    public ParticipantListField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, Class<E> type, boolean shared) {
        SOURCE = new ListField<>(fields,initializers , Field.Namespace.SOURCE, "Source " + name, type, shared, null);
        TARGET = new ListField<>(fields,initializers , Field.Namespace.TARGET, "Target " + name, type, shared, null);
    }
}
