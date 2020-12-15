package uk.ac.ebi.intact.app.internal.ui.panels.filters;

import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.model.filters.ContinuousFilter;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.components.buttons.HelpButton;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;

import static uk.ac.ebi.intact.app.internal.model.styles.UIColors.lightBackground;

public abstract class FilterPanel<F extends Filter<? extends Element>> extends CollapsablePanel {
    protected final Manager manager;
    protected EasyGBC layoutHelper = new EasyGBC();
    protected F filter;

    public FilterPanel(Manager manager, F filter) {
        super(filter.name, true);
        this.manager = manager;
        this.filter = filter;
        content.setLayout(new GridBagLayout());
        setBackground(lightBackground);
        LinePanel header = new LinePanel(0);
        header.setBackground(null);
        header.add(new JLabel(filter.name));
        header.add(new HelpButton(manager, filter.name, filter.definition));
        this.setHeader(header);
    }

    public F getFilter() {
        return filter;
    }

    public void setFilter(F filter) {
        this.filter = filter;
        updateFilter(filter);
    }

    protected abstract void updateFilter(F filter);

    public static <T extends Element> FilterPanel<?> createFilterPanel(Filter<T> filter, Manager manager) {
        if (filter instanceof ContinuousFilter) {
            return  new ContinuousFilterPanel<>(manager, (ContinuousFilter<T>) filter);
        } else if (filter instanceof DiscreteFilter) {
            return new DiscreteFilterPanel<>(manager, (DiscreteFilter<T>) filter);
        } else if (filter instanceof BooleanFilter) {
            return new BooleanFilterPanel<>(manager, (BooleanFilter<T>) filter);
        }
        return null;
    }
}
