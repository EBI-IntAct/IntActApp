package uk.ac.ebi.intact.intactApp.internal.utils;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;

import java.util.ArrayList;
import java.util.List;

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

        for (CyEdge edge: network.getEdgeList()) {
            if (network.getRow(edge).get(filteredColumnName, Object.class) != null) {
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
}
