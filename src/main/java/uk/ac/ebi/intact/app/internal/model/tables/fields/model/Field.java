package uk.ac.ebi.intact.app.internal.model.tables.fields.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;

import java.util.*;
import java.util.function.Consumer;

public class Field<T> implements FieldInitializer {
    public enum Namespace {
        NULL(""),
        INTACT("IntAct"),
        SUMMARY("IntAct - Summary"),
        SOURCE("IntAct - Source"),
        TARGET("IntAct - Target"),
        FEATURE("IntAct - Feature"),
        IDENTIFIER("IntAct - Identifier"),
        VIEW("IntAct - View");

        public final String name;

        Namespace(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final List<Field<?>> fields = new ArrayList<>();
    public static final Set<String> keys = new HashSet<>();
    public final Namespace namespace;
    public final String name;
    public final String jsonKey;
    public final Class<T> type;
    public final boolean shared;

    public final T defaultValue;

    public Field(Table table, Namespace namespace, String name, String jsonKey, Class<T> type) {
        this(table, namespace, name, jsonKey, type, true, null);
    }

    public Field(Table table, Namespace namespace, String name, String jsonKey, Class<T> type, boolean shared) {
        this(table, namespace, name, jsonKey, type, shared, null);
    }

    public Field(Table table, Namespace namespace, String name, String jsonKey, Class<T> type, T defaultValue) {
        this(table, namespace, name, jsonKey, type, true, defaultValue);
    }

    public Field(Table table, Namespace namespace, String name, String jsonKey, Class<T> type, boolean shared, T defaultValue) {
        this.namespace = namespace;
        this.name = name;
        this.jsonKey = jsonKey;
        this.type = type;
        this.shared = shared;
        this.defaultValue = defaultValue;
        fields.add(this);
        table.fields.add(this);
        table.initializers.add(this);
        if (jsonKey != null) {
            keys.add(jsonKey);
            table.keysToIgnore.add(jsonKey);
        }
    }

    @Override
    public void createColumn(CyTable table) {
        if (defaultValue == null) TableUtil.createColumnIfNeeded(table, type, toString());
        else TableUtil.createColumnIfNeeded(table, type, toString(), defaultValue);
    }

    @Override
    public void setValueFromJson(CyRow row, JsonNode json) {
        if (jsonKey == null) {
            if (defaultValue != null) setValue(row, defaultValue);
            return;
        }
        JsonNode node = json.get(jsonKey);
        if (node == null)
            throw new IllegalArgumentException(String.format("Given json does not have required field \"%s\"", jsonKey));

        if (type == String.class) setValue(row, type.cast(node.textValue()));
        else if (type == Short.class) setValue(row, type.cast(node.shortValue()));
        else if (type == Integer.class) setValue(row, type.cast(node.intValue()));
        else if (type == Long.class) setValue(row, type.cast(node.longValue()));
        else if (type == Float.class) setValue(row, type.cast(node.floatValue()));
        else if (type == Double.class) setValue(row, type.cast(node.doubleValue()));
        else if (type == Boolean.class) setValue(row, type.cast(node.booleanValue()));
    }

    @Override
    public boolean isDefinedIn(CyTable table) {
        return table != null && table.getColumn(toString()) != null;
    }


    @Override
    public boolean isShared() {
        return shared;
    }

    public CyColumn getColumn(CyTable table) {
        return table.getColumn(toString());
    }

    public Collection<CyRow> getMatchingRows(CyTable table, T value) {
        return table.getMatchingRows(toString(), value);
    }

    public Map<T, List<CyRow>> groupRows(CyTable table) {
        return CollectionUtils.groupBy(table.getAllRows(), this::getValue);
    }

    public void setValue(CyRow row, T value) {
        row.set(toString(), value);
    }

    public void setAllValues(CyTable table, T value) {
        if (!isDefinedIn(table)) return;
        for (CyRow row : table.getAllRows()) {
            setValue(row, value);
        }
    }

    public T getValue(CyRow row) {
        T value = row.get(toString(), type);
        if (value == null && defaultValue != null) {
            setValue(row, defaultValue);
            return defaultValue;
        }
        return value;
    }

    public List<T> getAllValues(CyTable table) {
        List<T> allValues = new ArrayList<>();
        if (!isDefinedIn(table)) return allValues;
        for (CyRow row : table.getAllRows()) {
            allValues.add(getValue(row));
        }
        return allValues;
    }

    public void forEachCell(CyTable table, Consumer<T> toApply) {
        getColumn(table).getValues(type).forEach(toApply);
    }

    @Override
    public String toString() {
        if (namespace == Namespace.NULL) return name;
        return namespace.toString() + "::" + name;
    }
}
