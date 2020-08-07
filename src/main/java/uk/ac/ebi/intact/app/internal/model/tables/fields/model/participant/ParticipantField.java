package uk.ac.ebi.intact.app.internal.model.tables.fields.model.participant;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.FieldInitializer;

import java.util.List;

public class ParticipantField<T> implements FieldInitializer {
    public final Field<T> SOURCE;
    public final Field<T> TARGET;

    public ParticipantField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String jsonKey, Class<T> type) {
        this(fields, initializers, name, jsonKey, type, true, null);
    }

    public ParticipantField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String jsonKey, Class<T> type, boolean shared) {
        this(fields, initializers, name, jsonKey, type, shared, null);
    }


    public ParticipantField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String jsonKey, Class<T> type, T defaultValue) {
        this(fields, initializers, name, jsonKey, type, true, defaultValue);
    }

    public ParticipantField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String jsonKey, Class<T> type, boolean shared, T defaultValue) {
        SOURCE = new Field<>(fields, initializers, Field.Namespace.SOURCE, "Source " + name, jsonKey, type, shared, defaultValue);
        TARGET = new Field<>(fields, initializers, Field.Namespace.TARGET, "Target " + name, jsonKey, type, shared, defaultValue);
        initializers.remove(SOURCE);
        initializers.remove(TARGET);
        initializers.add(this);
    }

    @Override
    public void createColumn(CyTable table) {
        SOURCE.createColumn(table);
        TARGET.createColumn(table);
    }

    @Override
    public void setValueFromJson(CyRow row, JsonNode json) {
        SOURCE.setValueFromJson(row, json.get("source"));
        TARGET.setValueFromJson(row, json.get("target"));
    }

    @Override
    public boolean isDefinedIn(CyTable table) {
        return SOURCE.isDefinedIn(table) && TARGET.isDefinedIn(table);
    }

    @Override
    public boolean isShared() {
        return SOURCE.shared;
    }
}
