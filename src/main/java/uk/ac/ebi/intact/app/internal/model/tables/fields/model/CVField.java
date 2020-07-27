package uk.ac.ebi.intact.app.internal.model.tables.fields.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.tables.Table;

import static uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field.Namespace;

public class CVField implements FieldInitializer {
    public final SourceOntology ontology;
    public final Field<String> VALUE;
    public final Field<String> ID;


    /**
     * Assume SourceOntology is MI
     * and
     * Assume id json key is formatted as "{valueJsonKey}_mi_identifier"
     */
    public CVField(Table table, Namespace namespace, String name, String jsonKey) {
        this(table, namespace, name, jsonKey, SourceOntology.MI);
    }

    /**
     * Assume id json key is formatted as "{valueJsonKey}_{Ontology.abbreviation.toLowerCase()}_identifier"
     */
    public CVField(Table table, Namespace namespace, String name, String jsonKey, SourceOntology ontology) {
        this(table, namespace, name, jsonKey, String.format("%s_%s_identifier", jsonKey, ontology.abbreviation.toLowerCase()), ontology);
    }

    public CVField(Table table, Namespace namespace, String name, String valueJsonKey, String idJsonKey, SourceOntology ontology) {
        this.ontology = ontology;
        VALUE = new Field<>(table, namespace, name, valueJsonKey, String.class);
        ID = new Field<>(table, namespace, String.format("%s %s identifier", name, ontology.abbreviation), idJsonKey, String.class);
        table.initializers.remove(VALUE);
        table.initializers.remove(ID);
        table.initializers.add(this);
    }

    public void createColumn(CyTable table) {
        VALUE.createColumn(table);
        ID.createColumn(table);
    }

    @Override
    public void setValueFromJson(CyRow row, JsonNode json) {
        VALUE.setValueFromJson(row, json);
        ID.setValueFromJson(row, json);
    }

    @Override
    public boolean isDefinedIn(CyTable table) {
        return VALUE.isDefinedIn(table) && ID.isDefinedIn(table);
    }

    @Override
    public boolean isShared() {
        return true;
    }
}
