package uk.ac.ebi.intact.app.internal.model.tables.fields.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public interface FieldInitializer {
    void createColumn(CyTable table);

    void setValueFromJson(CyRow row, JsonNode json);

    boolean isDefinedIn(CyTable table);

    boolean isShared();
}
