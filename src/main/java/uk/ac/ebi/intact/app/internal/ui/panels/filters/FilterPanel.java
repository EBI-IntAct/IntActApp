package uk.ac.ebi.intact.app.internal.ui.panels.filters;

import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.events.FilterUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.FilterUpdatedListener;
import uk.ac.ebi.intact.app.internal.model.filters.*;
import uk.ac.ebi.intact.app.internal.model.filters.edge.EdgePositiveFilter;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.components.buttons.HelpButton;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

import static uk.ac.ebi.intact.app.internal.model.styles.UIColors.lightBackground;

public abstract class FilterPanel<F extends Filter<? extends Element>> extends CollapsablePanel implements FilterUpdatedListener {
    protected final Manager manager;
    protected EasyGBC layoutHelper = new EasyGBC();
    protected F filter;
    private boolean listening = true;

    public FilterPanel(Manager manager, F filter) {
        super(filter.name, true);
        this.manager = manager;
        this.filter = filter;
        content.setLayout(new GridBagLayout());
        setBackground(lightBackground);
        LinePanel header = new LinePanel(0);
        header.setBackground(null);
        header.add(new JLabel(filter.name));
        header.add(new HelpButton(manager, filter.definition));
        this.setHeader(header);
        this.manager.utils.registerAllServices(this, new Properties());
    }

    public F getFilter() {
        return filter;
    }

    public void setFilter(F filter) {
        this.filter = filter;
        updateFilterUI(filter);
    }

    @Override
    public void handleEvent(FilterUpdatedEvent event) {
        if (listening && event.getFilter() == filter) updateFilterUI((F) event.getFilter());
    }

    protected abstract void updateFilterUI(F filter);

    public static <T extends Element> FilterPanel<?> createFilterPanel(Filter<T> filter, Manager manager) {
        if (filter instanceof ContinuousFilter) {
            return new ContinuousFilterPanel<>(manager, (ContinuousFilter<T>) filter);
        } else if (filter instanceof DiscreteFilter) {
            return new DiscreteFilterPanel<>(manager, (DiscreteFilter<T>) filter);
        } else if (filter instanceof BooleanFilter) {
            if (filter instanceof EdgePositiveFilter) {
                return new ToggleFilterPanel<>(manager, (EdgePositiveFilter) filter);
            }
            return new BooleanFilterPanel<>(manager, (BooleanFilter<T>) filter);
        } else if (filter instanceof RadioButtonFilter){
            System.out.println("Radio filter");
            return new RadioButtonFilterPanel<>(manager, (RadioButtonFilter<T>) filter);
        }
        return null;
    }

    protected boolean isListening() {
        return listening;
    }

    protected void setListening(boolean listening) {
        this.listening = listening;
    }
}
