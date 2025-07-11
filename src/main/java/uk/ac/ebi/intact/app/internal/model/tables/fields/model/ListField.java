package uk.ac.ebi.intact.app.internal.model.tables.fields.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class ListField<E> extends Field<List<E>> {
    public final Class<E> elementsType;

    public ListField(List<Field<?>> fields, List<FieldInitializer> initializers, Namespace namespace, String name, Class<E> elementsType) {
        this(fields, initializers, namespace, name, elementsType, true, null);
    }

    public ListField(List<Field<?>> fields, List<FieldInitializer> initializers, Namespace namespace, String name, Class<E> elementsType, String jsonKey) {
        this(fields, initializers, namespace, name, elementsType, true, jsonKey);
    }

    public ListField(List<Field<?>> fields, List<FieldInitializer> initializers, Namespace namespace, String name, Class<E> elementsType, boolean shared, String jsonKey) {
        super(fields, initializers, namespace, name, null, null, shared);
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

    public void addValues(CyRow row, Collection<E> values) {
        List<E> list = row.getList(toString(), elementsType);
        if (list != null) list.addAll(values);
        else row.set(toString(), values);
    }

    public boolean removeValue(CyRow row, E value) {
        List<E> list = row.getList(toString(), elementsType);
        if (list != null) return list.remove(value);
        else row.set(toString(), new ArrayList<>());
        return false;
    }

    public boolean removeValues(CyRow row, Collection<E> values) {
        List<E> list = row.getList(toString(), elementsType);
        if (list != null) return list.removeAll(values);
        else row.set(toString(), new ArrayList<>());
        return false;
    }

    @Override
    public List<E> getValue(CyRow row) {
        List<E> values = row.getList(toString(), elementsType);
        if (values == null) {
            setValue(row, new ArrayList<>());
            return getValue(row);
        }
        return values;
    }

    public void map(CyRow row, UnaryOperator<E> mapping) {
        setValue(row, getValue(row).stream().map(mapping).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public void filter(CyRow row, Predicate<E> filter) {
        setValue(row, getValue(row).stream().filter(filter).collect(Collectors.toList()));
    }

    public void forEachElement(CyRow row, Consumer<E> toApply) {
        getValue(row).forEach(toApply);
    }

    public void clear(CyRow row) {
        setValue(row, new ArrayList<>());
    }

    public void clearAllIn(CyTable table) {
        if (isDefinedIn(table)) table.getAllRows().forEach(this::clear);
    }
}