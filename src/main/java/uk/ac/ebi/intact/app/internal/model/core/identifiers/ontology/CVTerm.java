package uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.CVField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;

import java.util.Objects;

public class CVTerm implements Comparable<CVTerm>{
    public final String value;
    public final OntologyIdentifier id;

    public CVTerm(String value, OntologyIdentifier id) {
        this.value = value;
        this.id = id;
    }

    public CVTerm(CyRow row, CVField field) {
        this.value = field.VALUE.getValue(row);
        this.id = new OntologyIdentifier(row, field);
    }

    public CVTerm(CyRow row, Field<String> valueField, Field<String> miIdField, Field<String> modIdField, Field<String> parIdField) {
        this.value = valueField.getValue(row);
        this.id = TableUtil.getOntologyIdentifier(row, miIdField, modIdField, parIdField);
    }

    public void writeInTable(CyRow row, CVField field) {
        field.VALUE.setValue(row, value);
        field.ID.setValue(row, id.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CVTerm cvTerm = (CVTerm) o;
        return id.equals(cvTerm.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return value;
    }


    @Override
    public int compareTo(CVTerm o) {
        return value.compareTo(o.value);
    }
}
