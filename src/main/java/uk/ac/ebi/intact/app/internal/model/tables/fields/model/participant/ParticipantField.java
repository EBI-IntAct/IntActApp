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

    public ParticipantField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String jsonKey, Class<T> type, boolean isPublic) {
        this(fields, initializers, name, jsonKey, type, true, null, isPublic);
    }

    public ParticipantField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String jsonKey, Class<T> type, boolean shared, boolean isPublic) {
        this(fields, initializers, name, jsonKey, type, shared, null, isPublic);
    }


    public ParticipantField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String jsonKey, Class<T> type, T defaultValue, boolean isPublic) {
        this(fields, initializers, name, jsonKey, type, true, defaultValue, isPublic);
    }

    public ParticipantField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String jsonKey, Class<T> type, boolean shared, T defaultValue, boolean isPublic) {
        SOURCE = new Field<>(fields, initializers, Field.Namespace.SOURCE, "Source " + name, jsonKey, type, shared, isPublic, defaultValue);
        TARGET = new Field<>(fields, initializers, Field.Namespace.TARGET, "Target " + name, jsonKey, type, shared, isPublic, defaultValue);
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
