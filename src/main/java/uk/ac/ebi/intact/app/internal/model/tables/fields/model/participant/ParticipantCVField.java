package uk.ac.ebi.intact.app.internal.model.tables.fields.model.participant;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.CVField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.FieldInitializer;

import java.util.List;

public class ParticipantCVField implements FieldInitializer {
    public final CVField SOURCE;
    public final CVField TARGET;

    /**
     * Assume SourceOntology is MI
     * and
     * Assume id json key is formatted as "{valueJsonKey}_{Ontology.abbreviation.toLowerCase()}_identifier"
     */
    public ParticipantCVField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String jsonKey) {
        this(fields, initializers, name, jsonKey, SourceOntology.MI);
    }

    /**
     * Assume id json key is formatted as "{valueJsonKey}_{Ontology.abbreviation.toLowerCase()}_identifier"
     */
    public ParticipantCVField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String jsonKey, SourceOntology ontology) {
        this(fields, initializers, name, jsonKey, String.format("%s_%s_identifier", jsonKey, ontology.abbreviation.toLowerCase()), ontology);
    }

    public ParticipantCVField(List<Field<?>> fields, List<FieldInitializer> initializers, String name, String valueJsonKey, String idJsonKey, SourceOntology ontology) {
        SOURCE = new CVField(fields, initializers, Field.Namespace.SOURCE, "Source " + name, valueJsonKey, idJsonKey, ontology);
        TARGET = new CVField(fields, initializers, Field.Namespace.TARGET, "Target " + name, valueJsonKey, idJsonKey, ontology);
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
        return true;
    }
}
