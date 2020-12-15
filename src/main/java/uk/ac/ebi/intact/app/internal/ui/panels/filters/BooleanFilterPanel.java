package uk.ac.ebi.intact.app.internal.ui.panels.filters;

import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.components.buttons.ToggleSwitch;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class BooleanFilterPanel<T extends Element> extends FilterPanel<BooleanFilter<T>> implements ChangeListener {
    private final ToggleSwitch toggleSwitch = new ToggleSwitch(false, new Color(34, 83, 157));
    private final JLabel label = new JLabel("");

    public BooleanFilterPanel(Manager manager, BooleanFilter<T> filter) {
        super(manager, filter);
        content.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        content.add(toggleSwitch);
        toggleSwitch.addChangeListener(this);
        content.add(label);
        updateFilter(filter);
    }

    @Override
    protected void updateFilter(BooleanFilter<T> filter) {
        label.setText(filter.description);
        toggleSwitch.setActivated(filter.getStatus());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        filter.setStatus(toggleSwitch.isActivated());
    }
}
