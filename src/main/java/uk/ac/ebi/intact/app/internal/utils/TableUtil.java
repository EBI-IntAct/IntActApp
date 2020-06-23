package uk.ac.ebi.intact.app.internal.utils;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.model.core.ontology.OntologyIdentifier;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.ebi.intact.app.internal.model.core.ontology.SourceOntology.*;

public class TableUtil {
    public static <T> List<T> getColumnValuesOfEdges(CyTable table, String columnName, Class<? extends T> columnType, List<CyEdge> edges, T defaultValue) {
        List<T> columnValues = new ArrayList<>();
        for (CyEdge edge : edges) {
            columnValues.add(table.getRow(edge.getSUID()).get(columnName, columnType, defaultValue));
        }
        return columnValues;
    }

    public static NullAndNonNullEdges splitNullAndNonNullEdges(CyNetwork network, String filteredColumnName) {
        NullAndNonNullEdges result = new NullAndNonNullEdges();

        for (CyEdge edge : network.getEdgeList()) {
            String value = network.getRow(edge).get(filteredColumnName, String.class);
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

    public static OntologyIdentifier getOntologyIdentifier(CyRow row, String miColumn, String modColumn, String parColumn) {
        String mi = row.get(miColumn, String.class);
        if (mi != null && !mi.isBlank()) {
            return new OntologyIdentifier(mi, MI);
        } else {
            String mod = row.get(modColumn, String.class);
            if (mod != null && !mod.isBlank()) {
                return new OntologyIdentifier(mod, MOD);
            } else {
                String par = row.get(parColumn, String.class);
                return new OntologyIdentifier(par, PAR);
            }
        }
    }

    public static OntologyIdentifier getOntologyIdentifier(String miID, String modID, String parID) {
        if (modID != null && !modID.isBlank()) return new OntologyIdentifier(modID, MOD);
        else if (miID != null && !miID.isBlank()) return new OntologyIdentifier(miID, MI);
        else return new OntologyIdentifier(parID, PAR);
    }
}

