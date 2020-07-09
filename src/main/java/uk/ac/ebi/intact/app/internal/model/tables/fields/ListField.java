package uk.ac.ebi.intact.app.internal.model.tables.fields;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ListField<E> extends Field<List<E>> {
    public final Class<E> elementsType;

    public ListField(Table table, Namespace namespace, String name, Class<E> elementsType) {
        this(table, namespace, name, elementsType, true);
    }

    public ListField(Table table, Namespace namespace, String name, Class<E> elementsType, boolean shared) {
        super(table, namespace, name, null, null, shared);
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
        if (list != null) {
            if (!list.contains(value)) list.add(value);
        } else {
            row.set(toString(), List.of(value));
        }
    }

    public void addValues(CyRow row, List<E> values) {
        List<E> list = row.getList(toString(), elementsType);
        if (list != null) list.addAll(values);
        else row.set(toString(), values);
    }

    @Override
    public List<E> getValue(CyRow row) {
        List<E> values = row.getList(toString(), elementsType);
        if (values  == null) {
            setValue(row, new ArrayList<>());
            return getValue(row);
        }
        return values;
    }

    public void map(CyRow row, UnaryOperator<E> mapping) {
        List<E> values = getValue(row);
        if (values != null) setValue(row, values.stream().map(mapping).collect(Collectors.toList()));
    }

    public void filter(CyRow row, Predicate<E> filter) {
        List<E> values = getValue(row);
        if (values != null) setValue(row, values.stream().filter(filter).collect(Collectors.toList()));
    }
}