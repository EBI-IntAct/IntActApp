package uk.ac.ebi.intact.app.internal.model.tables.fields;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;
import uk.ac.ebi.intact.app.internal.model.tables.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Field<T> {
    public enum Namespace {
        NULL(""),
        INTACT("IntAct"),
        COLLAPSED("Collapsed"),
        SOURCE("Source"),
        TARGET("Target"),
        FEATURE("Feature"),
        IDENTIFIER("Identifier"),
        VIEW("View");

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

    public final T defaultValue;

    public Field(Table table, Namespace namespace, String name, String jsonKey, Class<T> type) {
        this(table, namespace, name, jsonKey, type, null);
    }

    public Field(Table table, Namespace namespace, String name, String jsonKey, Class<T> type, T defaultValue) {
        this.namespace = namespace;
        this.name = name;
        this.jsonKey = jsonKey;
        this.type = type;
        this.defaultValue = defaultValue;
        fields.add(this);
        table.fields.add(this);
        if (jsonKey != null) {
            keys.add(jsonKey);
            table.keysToIgnore.add(jsonKey);
        }
    }

    public void createColumn(CyTable table) {
        if (defaultValue == null) TableUtil.createColumnIfNeeded(table, type, toString());
        else TableUtil.createColumnIfNeeded(table, type, toString(), defaultValue);
    }

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

    public boolean isDefinedIn(CyTable table) {
        return table.getColumn(toString()) != null;
    }

    public CyColumn getColumn(CyTable table) {
        return table.getColumn(toString());
    }

    public void setValue(CyRow row, T value) {
        row.set(toString(), value);
    }

    public T getValue(CyRow row) {
        return row.get(toString(), type);
    }

    @Override
    public String toString() {
        if (namespace == Namespace.NULL) return name;
        return namespace.toString() + "::" + name;
    }
}
