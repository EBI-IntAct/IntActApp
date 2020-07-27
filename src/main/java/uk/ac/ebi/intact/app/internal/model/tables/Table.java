package uk.ac.ebi.intact.app.internal.model.tables;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.FieldInitializer;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;

import java.util.*;
import java.util.stream.Collectors;

public enum Table {
    NODE("identifiers", "label"),
    EDGE("source", "target"),
    NETWORK,
    FEATURE,
    IDENTIFIER;

    public final List<FieldInitializer> initializers = new ArrayList<>();
    public final List<Field<?>> fields = new ArrayList<>();
    public final Set<String> keysToIgnore = new HashSet<>();


    Table(String... keysToIgnore) {
        List<String> keys = Arrays.asList(keysToIgnore);
        this.keysToIgnore.addAll(keys);
        Field.keys.addAll(keys);
    }


    public void setRowFromJson(CyRow row, JsonNode data) {
        for (FieldInitializer initializer : initializers) {
            if (!(initializer instanceof ListField)) initializer.setValueFromJson(row, data);
        }
    }

    public void initTable(CyTable sharedTable, CyTable localTable) {
        for (FieldInitializer initializer : initializers) {
            initializer.createColumn(initializer.isShared() ? sharedTable : localTable);
        }
    }

    public boolean containsAllFields(CyTable table) {
        Set<String> columnNames = table.getColumns().stream().map(CyColumn::getName).collect(Collectors.toSet());
        return columnNames.size() >= fields.size() && fields.stream().map(Field::toString).allMatch(columnNames::contains);
    }
}


