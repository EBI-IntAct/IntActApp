package uk.ac.ebi.intact.app.internal.model.tables;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.*;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.*;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.FieldInitializer;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;

import java.util.*;
import java.util.stream.Collectors;

import static org.cytoscape.model.CyTableFactory.InitialTableSize.MEDIUM;
import static org.cytoscape.model.CyTableFactory.InitialTableSize.SMALL;

public enum Table {
    NODE(NodeFields.SUID, NodeFields.fields, NodeFields.initializers, true, true, MEDIUM, "identifiers", "label"),
    EDGE(EdgeFields.SUID, EdgeFields.fields, EdgeFields.initializers, true, true, MEDIUM, "source", "target"),
    NETWORK(NetworkFields.SUID, NetworkFields.fields, NetworkFields.initializers, true, true, SMALL),
    FEATURE(FeatureFields.AC, FeatureFields.fields, FeatureFields.initializers, true, true, MEDIUM),
    IDENTIFIER(IdentifierFields.AC, IdentifierFields.fields, IdentifierFields.initializers, true, true, MEDIUM),
    STYLE_SETTINGS(StyleSettingsFields.TYPE, StyleSettingsFields.fields, StyleSettingsFields.initializers, false, true, SMALL);

    public final Field<?> primaryKey;
    public final boolean isPublic;
    public final boolean isMutable;
    public final CyTableFactory.InitialTableSize initialSize;
    public final List<FieldInitializer> initializers;
    public final List<Field<?>> fields;
    public final Set<String> keysToIgnore = new HashSet<>();


    Table(Field<?> primaryKey, List<Field<?>> fields, List<FieldInitializer> initializers, boolean isPublic, boolean isMutable, CyTableFactory.InitialTableSize initialSize, String... keysToIgnore) {
        this.primaryKey = primaryKey;
        this.isPublic = isPublic;
        this.isMutable = isMutable;
        this.initialSize = initialSize;
        this.fields = fields;
        this.initializers = initializers;
        List<String> keys = Arrays.asList(keysToIgnore);
        this.keysToIgnore.addAll(keys);
        Field.keys.addAll(keys);
        fields.forEach(field -> {
            if (field.jsonKey != null) this.keysToIgnore.add(field.jsonKey);
        });
    }

    public CyTable getByTitle(Collection<CyTable> tableCollection, String title) {
        for (CyTable table : tableCollection) {
            if (table.getTitle().equals(title) && containsAllFields(table)) {
                return table;
            }
        }
        return null;
    }

    public CyTable getOrBuild(Manager manager, String title) {
        return getOrBuild(manager, title, manager.utils.getService(CyTableManager.class).getAllTables(true));
    }

    public CyTable getOrBuild(Manager manager, String title, Collection<CyTable> tableCollection) {
        CyTable table = getByTitle(tableCollection, title);
        return table != null ? table : build(manager, title);
    }

    public CyTable build(Manager manager, String title) {
        var tableFactory = manager.utils.getService(CyTableFactory.class);
        CyTable table = tableFactory.createTable(title, primaryKey.name, primaryKey.type, isPublic, isMutable, initialSize);
        initColumns(table, table);
        return table;
    }

    public void setRowFromJson(CyRow row, JsonNode data) {
        for (FieldInitializer initializer : initializers) {
            if (!(initializer instanceof ListField)) initializer.setValueFromJson(row, data);
        }
    }

    public void initColumns(CyTable sharedTable, CyTable localTable) {
        for (FieldInitializer initializer : initializers) {
            initializer.createColumn(initializer.isShared() ? sharedTable : localTable);
        }
    }

    public boolean containsAllFields(CyTable table) {
        Set<String> columnNames = table.getColumns().stream().map(CyColumn::getName).collect(Collectors.toSet());
        return columnNames.size() >= fields.size() && fields.stream().map(Field::toString).allMatch(columnNames::contains);
    }
}


