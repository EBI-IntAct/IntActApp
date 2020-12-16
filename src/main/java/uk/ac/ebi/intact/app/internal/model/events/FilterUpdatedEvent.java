package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;

public class FilterUpdatedEvent extends AbstractCyEvent<Filter<? extends Element>> {
    private final Filter<? extends Element> filter;

    public FilterUpdatedEvent(Filter<? extends Element> filter) {
        super(filter, FilterUpdatedListener.class);
        this.filter = filter;
    }

    public Filter<? extends Element> getFilter() {
        return filter;
    }
}
