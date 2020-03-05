package uk.ac.ebi.intact.intactApp.internal.utils.styles.FunctionalMapping;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.mappings.AbstractVisualMappingFunction;

import java.util.function.Function;

public class FunctionalMappingImpl<K, V> extends AbstractVisualMappingFunction<K, V> implements FunctionalMapping<K, V> {

    private Function<K, V> function;

    public FunctionalMappingImpl(String columnName, Class<K> columnType, VisualProperty<V> vp, CyEventHelper eventHelper) {
        super(columnName, columnType, vp, eventHelper);
    }


    @Override
    public V getMappedValue(CyRow row) {
        if (row == null || !row.isSet(columnName))
            return null;

        K tableValue = null;
        final CyColumn column = row.getTable().getColumn(columnName);

        if (column != null) {
            // Always try to find the data type from the current table/column first
            final Class<?> columnClass = column.getType();

            try {
                tableValue = (K) row.get(columnName, columnClass);
            } catch (ClassCastException cce) {
                return null;
            }
            return function.apply(tableValue);
        }
        return null;
    }

    @Override
    public String toString() {
        return FUNCTIONAL;
    }

    public Function<K, V> getFunction() {
        return function;
    }

    public void setFunction(Function<K, V> function) {
        this.function = function;
    }
}