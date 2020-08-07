package uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology;

import org.cytoscape.model.CyRow;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.CVField;

import java.util.Objects;

import static uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology.*;

public class OntologyIdentifier {
    public final SourceOntology sourceOntology;
    public final String id;

    public OntologyIdentifier(String id) {
        this.id = id;
        if (id.startsWith("MI")) {
            sourceOntology = MI;
        } else if (id.startsWith("MOD")) {
            sourceOntology = MOD;
        } else {
            sourceOntology = PAR;
        }
    }

    public OntologyIdentifier(String id, SourceOntology sourceOntology) {
        this.sourceOntology = sourceOntology;
        this.id = id;
    }

    public OntologyIdentifier(CyRow row, CVField field) {
        this.sourceOntology = field.ontology;
        this.id = field.ID.getValue(row);
    }

    @Override
    public String toString() {
        return id + " of " + sourceOntology;
    }

    public String getUserAccessURL() {
        return sourceOntology.getUserAccessURL(id);
    }

    public String getDetailsURL() {
        return sourceOntology.getDetailsURL(id);
    }

    public String getDescendantsURL() {
        return sourceOntology.getDescendantsURL(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OntologyIdentifier that = (OntologyIdentifier) o;
        return sourceOntology == that.sourceOntology &&
                id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceOntology, id);
    }
}
