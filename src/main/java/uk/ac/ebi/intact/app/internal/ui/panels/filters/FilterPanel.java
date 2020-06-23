package uk.ac.ebi.intact.app.internal.ui.panels.filters;

import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.model.filters.ContinuousFilter;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import java.awt.*;

import static uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractDetailPanel.backgroundColor;

public abstract class FilterPanel<F extends Filter<? extends Element>> extends CollapsablePanel {
    protected EasyGBC layoutHelper = new EasyGBC();
    protected F filter;

    public FilterPanel(F filter) {
        super(filter.name, true);
        this.filter = filter;
        content.setLayout(new GridBagLayout());
        setBackground(backgroundColor);
    }

    public F getFilter() {
        return filter;
    }

    public void setFilter(F filter) {
        this.filter = filter;
        updateFilter(filter);
    }

    protected abstract void updateFilter(F filter);

    public static <T extends Element> FilterPanel<?> createFilterPanel(Filter<T> filter) {
        if (filter instanceof ContinuousFilter) {
            return  new ContinuousFilterPanel<T>((ContinuousFilter<T>) filter);
        } else if (filter instanceof DiscreteFilter) {
            return new DiscreteFilterPanel<T>((DiscreteFilter<T>) filter);
        } else if (filter instanceof BooleanFilter) {
            return new BooleanFilterPanel<T>((BooleanFilter<T>) filter);
        }
        return null;
    }
}
