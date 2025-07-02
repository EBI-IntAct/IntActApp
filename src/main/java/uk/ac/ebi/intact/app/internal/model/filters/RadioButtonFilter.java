package uk.ac.ebi.intact.app.internal.model.filters;

import lombok.Getter;
import lombok.Setter;

import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class RadioButtonFilter<T extends Element> extends Filter<T>  {
    private String groupingField;
    private String currentSelectedProperty;
    private String defaultSelectedProperty;
    @Getter
    private Set<String> properties = new HashSet<>();

    public RadioButtonFilter(NetworkView view, String name, String definition, Class<T> elementType, String groupingField, String currentSelectedProperty) {
        super(view, name, definition, elementType);
        this.groupingField = groupingField;
        this.currentSelectedProperty = currentSelectedProperty;
        this.defaultSelectedProperty = currentSelectedProperty;
        setProperties();
    }

    @Override
    public void filterView() {
    }

    @Override
    public void reset() {
        this.currentSelectedProperty = defaultSelectedProperty;
    }

    public void setProperties(){
        Network network = super.getNetwork();
        properties.clear();
        properties = network.getDatabases(groupingField);
    }
}
