package uk.ac.ebi.intact.app.internal.utils;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.Field;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology.*;

public class TableUtil {
    public static <T> List<T> getFieldValuesOfEdges(CyTable table, Field<T> field, List<CyEdge> edges, T defaultValue) {
        List<T> fieldValues = new ArrayList<>();
        for (CyEdge edge : edges) {
            T value = field.getValue(table.getRow(edge.getSUID()));
            if (value == null) value = defaultValue;
            fieldValues.add(value);
        }
        return fieldValues;
    }


    public static NullAndNonNullEdges splitNullAndNonNullEdges(CyNetwork network, Field<String> keyFilter) {
        NullAndNonNullEdges result = new NullAndNonNullEdges();

        for (CyEdge edge : network.getEdgeList()) {
            String value = keyFilter.getValue(network.getRow(edge));
            if (value != null && !value.isBlank()) {
                result.nonNullEdges.add(edge);
            } else {
                result.nullEdges.add(edge);
            }
        }
        return result;
    }

    public static class NullAndNonNullEdges {
        public final List<CyEdge> nonNullEdges = new ArrayList<>();
        public final List<CyEdge> nullEdges = new ArrayList<>();
    }

    public static OntologyIdentifier getOntologyIdentifier(CyRow row, Field<String> miColumn, Field<String> modColumn, Field<String> parColumn) {
        String mi = miColumn.getValue(row);
        if (mi != null && !mi.isBlank()) {
            return new OntologyIdentifier(mi, MI);
        } else {
            String mod = modColumn.getValue(row);
            if (mod != null && !mod.isBlank()) {
                return new OntologyIdentifier(mod, MOD);
            } else {
                String par = parColumn.getValue(row);
                return new OntologyIdentifier(par, PAR);
            }
        }
    }
}

