package uk.ac.ebi.intact.intactApp.internal.ui.panels.filters;

import uk.ac.ebi.intact.intactApp.internal.model.core.IntactElement;
import uk.ac.ebi.intact.intactApp.internal.model.filters.BooleanFilter;
import uk.ac.ebi.intact.intactApp.internal.ui.components.ToggleSwitch;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class BooleanFilterPanel<T extends IntactElement> extends FilterPanel<BooleanFilter<T>> implements ChangeListener {
    private final ToggleSwitch toggleSwitch = new ToggleSwitch(false, new Color(34, 83, 157));
    private final JLabel label = new JLabel("");

    public BooleanFilterPanel(BooleanFilter<T> filter) {
        super(filter);
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
