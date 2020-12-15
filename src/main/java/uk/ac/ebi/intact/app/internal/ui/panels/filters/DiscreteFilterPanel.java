package uk.ac.ebi.intact.app.internal.ui.panels.filters;

import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static uk.ac.ebi.intact.app.internal.model.styles.UIColors.lightBackground;

public class DiscreteFilterPanel<T extends Element> extends FilterPanel<DiscreteFilter<T>> {

    List<JCheckBox> checkBoxes = new ArrayList<>();

    public DiscreteFilterPanel(Manager manager, DiscreteFilter<T> filter) {
        super(manager, filter);
        buildOptionLines();
    }

    public void updateFilter(DiscreteFilter<T> filter) {
        content.removeAll();
        checkBoxes.clear();
        buildOptionLines();
    }

    private void buildOptionLines() {
        JButton selectAll = new JButton("Select all");
        selectAll.addActionListener(e -> {
            NetworkView view = filter.getNetworkView();
            view.silenceFilters(true);
            filter.getPropertiesVisibility().keySet().forEach(s -> filter.setPropertyVisibility(s, true));
            checkBoxes.forEach(jCheckBox -> jCheckBox.setSelected(true));
            view.silenceFilters(false);
            view.filter();
        });
        JButton selectNone = new JButton("Select none");
        selectNone.addActionListener(e -> {
            NetworkView view = filter.getNetworkView();
            view.silenceFilters(true);
            filter.getPropertiesVisibility().keySet().forEach(s -> filter.setPropertyVisibility(s, false));
            checkBoxes.forEach(jCheckBox -> jCheckBox.setSelected(false));
            view.silenceFilters(false);
            view.filter();
        });
        LinePanel buttonsPanel = new LinePanel(lightBackground);
        buttonsPanel.add(selectAll);
        buttonsPanel.add(selectNone);
        content.add(buttonsPanel, layoutHelper.down().expandHoriz());
        filter.getPropertiesVisibility().keySet().stream().sorted(Comparator.nullsFirst(Comparator.naturalOrder())).forEach((value) -> {
            JCheckBox checkBox = new JCheckBox(value, filter.getPropertyVisibility(value));
            checkBoxes.add(checkBox);
            checkBox.addActionListener(e -> filter.setPropertyVisibility(value, checkBox.isSelected()));
            content.add(checkBox, layoutHelper.down().expandHoriz());
        });
    }
}
