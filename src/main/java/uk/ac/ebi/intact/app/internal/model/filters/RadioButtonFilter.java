package uk.ac.ebi.intact.app.internal.model.filters;

import lombok.Getter;
import lombok.Setter;

import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;

@Getter
@Setter
public class RadioButtonFilter<T extends Element> extends Filter<T>  {
    private String property;
    private String currentSelectedDb;
    private String defaultSelectedDb;

    public RadioButtonFilter(NetworkView view, String name, String definition, Class<T> elementType, String property, String currentSelectedDb) {
        super(view, name, definition, elementType);
        this.property = property;
        this.currentSelectedDb = currentSelectedDb;
        this.defaultSelectedDb = currentSelectedDb;
    }

    @Override
    public void filterView() {
//        manager.data.getCurrentNetwork().collapseGroups(property, currentSelectedDb);
    }

    @Override
    public void reset() {
        this.currentSelectedDb = defaultSelectedDb;
    }
}
