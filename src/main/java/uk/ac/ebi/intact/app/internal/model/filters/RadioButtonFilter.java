package uk.ac.ebi.intact.app.internal.model.filters;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public abstract class RadioButtonFilter<T extends Element> extends Filter<T>  {
    private ListField<String> groupingField;
    private String currentSelectedProperty;
    private String defaultSelectedProperty;
    private Set<String> properties;

    public RadioButtonFilter(NetworkView view, String name, String definition, Class<T> elementType, ListField<String> groupingField, String currentSelectedProperty) {
        super(view, name, definition, elementType);
        this.groupingField = groupingField;
        this.currentSelectedProperty = currentSelectedProperty;
        this.defaultSelectedProperty = currentSelectedProperty;
        this.properties = new HashSet<>();
        setProperties();
    }

    @Override
    public void filterView() {
    }

    @Override
    public void reset() {
        this.currentSelectedProperty = defaultSelectedProperty;
    }

    public abstract void setProperties();
}
