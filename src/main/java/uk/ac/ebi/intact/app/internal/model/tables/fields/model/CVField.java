package uk.ac.ebi.intact.app.internal.model.tables.fields.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;

import java.util.List;

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
    public CVField(List<Field<?>> fields, List<FieldInitializer> initializers, Namespace namespace, String name,
                   String jsonKey, boolean isPublic) {
        this(fields, initializers, namespace, name, jsonKey, SourceOntology.MI, isPublic);
    }

    /**
     * Assume id json key is formatted as "{valueJsonKey}_{Ontology.abbreviation.toLowerCase()}_identifier"
     */
    public CVField( List<Field<?>> fields, List<FieldInitializer> initializers, Namespace namespace, String name,
                    String jsonKey, SourceOntology ontology, boolean isPublic) {
        this(fields, initializers, namespace, name, jsonKey, String.format("%s_%s_identifier", jsonKey, ontology.abbreviation.toLowerCase()), ontology, isPublic);
    }

    public CVField(List<Field<?>> fields, List<FieldInitializer> initializers, Namespace namespace, String name,
                   String valueJsonKey, String idJsonKey, SourceOntology ontology, boolean isPublic) {
        this.ontology = ontology;
        VALUE = new Field<>(fields, initializers, namespace, name, valueJsonKey, String.class, isPublic);
        ID = new Field<>(fields, initializers, namespace, String.format("%s %s identifier", name, ontology.abbreviation), idJsonKey, String.class, isPublic);
        initializers.remove(VALUE);
        initializers.remove(ID);
        initializers.add(this);
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
