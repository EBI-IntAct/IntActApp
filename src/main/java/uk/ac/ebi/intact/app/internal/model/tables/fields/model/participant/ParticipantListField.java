package uk.ac.ebi.intact.app.internal.model.tables.fields.model.participant;

import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;

public class ParticipantListField<E> {
    public final ListField<E> SOURCE;
    public final ListField<E> TARGET;

    public ParticipantListField(Table table, String name, Class<E> type) {
        this(table, name, type, false);
    }

    public ParticipantListField(Table table, String name, Class<E> type, boolean shared) {
        SOURCE = new ListField<>(table, Field.Namespace.SOURCE, "Source " + name, type, shared);
        TARGET = new ListField<>(table, Field.Namespace.TARGET, "Target " + name, type, shared);
    }
}
