package uk.ac.ebi.intact.app.internal.model.tables;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.model.tables.fields.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.ListField;

import java.util.*;

public enum Table {
    NODE("identifiers", "label"),
    EDGE("source", "target"),
    NETWORK,
    FEATURE,
    IDENTIFIER;

    public final List<Field<?>> fields = new ArrayList<>();
    public final Set<String> keysToIgnore = new HashSet<>();


    Table(String... keysToIgnore) {
        List<String> keys = Arrays.asList(keysToIgnore);
        this.keysToIgnore.addAll(keys);
        Field.keys.addAll(keys);
    }


    public void setRowFromJson(CyRow row, JsonNode data) {
        for (Field<?> field : fields) {
            if (!(field instanceof ListField)) field.setValueFromJson(row, data);
        }
    }

    public void initTable(CyTable table) {
        for (Field<?> field : fields) {
            field.createColumn(table);
        }
    }
}

