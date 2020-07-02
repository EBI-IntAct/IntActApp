package uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.model.tables.fields.Field;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;

import java.util.Objects;

public class CVTerm implements Comparable<CVTerm>{
    public final String value;
    public final OntologyIdentifier id;

    public CVTerm(String value, OntologyIdentifier id) {
        this.value = value;
        this.id = id;
    }

    public CVTerm(CyRow row, Field<String> valueField, Field<String> idField) {
        this.value = valueField.getValue(row);
        this.id = new OntologyIdentifier(idField.getValue(row));
    }

    public CVTerm(CyRow row, Field<String> valueField, Field<String> idField, SourceOntology source) {
        this.value = valueField.getValue(row);
        this.id = new OntologyIdentifier(idField.getValue(row), source);
    }

    public CVTerm(CyRow row, Field<String> valueField, Field<String> miIdField, Field<String> modIdField, Field<String> parIdField) {
        this.value = valueField.getValue(row);
        this.id = TableUtil.getOntologyIdentifier(row, miIdField, modIdField, parIdField);
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
