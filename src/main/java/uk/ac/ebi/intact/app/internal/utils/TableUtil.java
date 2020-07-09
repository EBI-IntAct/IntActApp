package uk.ac.ebi.intact.app.internal.utils;

import org.cytoscape.model.*;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.tables.fields.Field;

import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology.*;

public class TableUtil {
    public static <T> List<T> getFieldValuesOfEdges(CyTable table, Field<T> field, List<CyEdge> edges, T defaultValue) {
        List<T> fieldValues = new ArrayList<>();
        for (CyEdge edge : edges) {
            T value = field.getValue(table.getRow(edge.getSUID()));
            if (value == null) value = defaultValue;
            fieldValues.add(value);
        }
        return fieldValues;
    }


    public static NullAndNonNullEdges splitNullAndNonNullEdges(CyNetwork network, Field<String> keyFilter) {
        NullAndNonNullEdges result = new NullAndNonNullEdges();

        for (CyEdge edge : network.getEdgeList()) {
            String value = keyFilter.getValue(network.getRow(edge));
            if (value != null && !value.isBlank()) {
                result.nonNullEdges.add(edge);
            } else {
                result.nullEdges.add(edge);
            }
        }
        return result;
    }

    public static class NullAndNonNullEdges {
        public final List<CyEdge> nonNullEdges = new ArrayList<>();
        public final List<CyEdge> nullEdges = new ArrayList<>();
    }

    public static void createColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            return;
        table.createColumn(columnName, clazz, false);
    }

    public static <T> void createColumnIfNeeded(CyTable table, Class<T> clazz, String columnName, T defaultValue) {
        if (table.getColumn(columnName) != null)
            return;
        table.createColumn(columnName, clazz, false, defaultValue);
    }

    public static void replaceColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            table.deleteColumn(columnName);

        table.createColumn(columnName, clazz, false);
    }

    public static void createListColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            return;

        table.createListColumn(columnName, clazz, false);
    }

    public static void replaceListColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            table.deleteColumn(columnName);

        table.createListColumn(columnName, clazz, false);
    }

    public static void deleteColumnIfExisting(CyTable table, String columnName) {
        if (table.getColumn(columnName) != null)
            table.deleteColumn(columnName);
    }

    public static String getName(CyNetwork network, CyIdentifiable ident) {
        return getString(network, ident, CyNetwork.NAME);
    }

    public static String getString(CyNetwork network, CyIdentifiable ident, String column) {
        if (network.getRow(ident, CyNetwork.DEFAULT_ATTRS) != null)
            return network.getRow(ident, CyNetwork.DEFAULT_ATTRS).get(column, String.class);
        return null;
    }

    public static OntologyIdentifier getOntologyIdentifier(CyRow row, Field<String> miColumn, Field<String> modColumn, Field<String> parColumn) {
        String mi = miColumn.getValue(row);
        if (mi != null && !mi.isBlank()) {
            return new OntologyIdentifier(mi, MI);
        } else {
            String mod = modColumn.getValue(row);
            if (mod != null && !mod.isBlank()) {
                return new OntologyIdentifier(mod, MOD);
            } else {
                String par = parColumn.getValue(row);
                return new OntologyIdentifier(par, PAR);
            }
        }
    }

    public static void copyRow(CyTable fromTable, CyTable toTable, Object fromPrimaryKey, Object toPrimaryKey, Set<String> fieldsToExclude) {
        Set<String> nonNullFieldsToExclude = (fieldsToExclude != null) ? new HashSet<>(fieldsToExclude) : new HashSet<>();
        nonNullFieldsToExclude.add(CyIdentifiable.SUID);
        Map<String, Class<?>> fromColumnNames = fromTable.getColumns().stream().filter(column -> !nonNullFieldsToExclude.contains(column.getName())).collect(Collectors.toMap(CyColumn::getName, CyColumn::getType));
        Map<String, Class<?>> toColumnNames = toTable.getColumns().stream().filter(column -> !nonNullFieldsToExclude.contains(column.getName())).collect(Collectors.toMap(CyColumn::getName, CyColumn::getType));
        if (!toColumnNames.keySet().containsAll(fromColumnNames.keySet()))
            return;
        if (fromTable.getPrimaryKey().getType() != fromPrimaryKey.getClass())
            return;
        if (toTable.getPrimaryKey().getType() != toPrimaryKey.getClass())
            return;
        CyRow fromRow = fromTable.getRow(fromPrimaryKey);
        CyRow toRow = toTable.getRow(toPrimaryKey);
        fromColumnNames.forEach((columnName, type) -> toRow.set(columnName, type != List.class ? fromRow.get(columnName, type) : fromRow.getList(columnName, fromTable.getColumn(columnName).getListElementType())));
    }
}

