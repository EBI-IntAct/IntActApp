package uk.ac.ebi.intact.app.internal.model.tables.fields;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;
import uk.ac.ebi.intact.app.internal.model.tables.Table;

import java.util.List;

public class ListField<E> extends Field<List<E>> {
    public final Class<E> elementsType;

    public ListField(Table table, Namespace namespace, String name, Class<E> elementsType) {
        super(table, namespace, name, null, null);
        this.elementsType = elementsType;
    }

    @Override
    @Deprecated
    public void setValueFromJson(CyRow row, JsonNode json) {
        throw new IllegalStateException("Cannot build list from json directly");
    }

    @Override
    public void createColumn(CyTable table) {
        TableUtil.createListColumnIfNeeded(table, elementsType, toString());
    }

    public void addValue(CyRow row, E value) {
        addValues(row, List.of(value));
    }

    public void addValueIfAbsent(CyRow row, E value) {
        List<E> list = row.getList(toString(), elementsType);
        if (list != null && !list.contains(value)) list.add(value);
        else row.set(toString(), List.of(value));
    }

    public void addValues(CyRow row, List<E> values) {
        List<E> list = row.getList(toString(), elementsType);
        if (list != null) list.addAll(values);
        else row.set(toString(), values);
    }

    @Override
    public List<E> getValue(CyRow row) {
        return row.getList(toString(), elementsType);
    }
}