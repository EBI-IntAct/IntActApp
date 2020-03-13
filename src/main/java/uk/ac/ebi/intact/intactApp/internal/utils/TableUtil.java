package uk.ac.ebi.intact.intactApp.internal.utils;

import org.cytoscape.model.CyEdge;
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
}
