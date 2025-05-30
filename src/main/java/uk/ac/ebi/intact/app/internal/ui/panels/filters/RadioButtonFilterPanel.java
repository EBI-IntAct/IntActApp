package uk.ac.ebi.intact.app.internal.ui.panels.filters;

import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.filters.RadioButtonFilter;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Set;

public class RadioButtonFilterPanel <T extends Element> extends FilterPanel<RadioButtonFilter<T>> implements ChangeListener {
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final Set<String> properties;
    private String selectedProperty;

    public RadioButtonFilterPanel(Manager manager, RadioButtonFilter<T> filter) {
        super(manager, filter);
        this.properties = manager.data.getCurrentNetwork().getOrhtologyDbs();
        this.selectedProperty = filter.getCurrentSelectedDb();

        buildRadioButtons();
        updateFilterUI(filter);
    }

    private void buildRadioButtons() {
        JPanel buttonPanel = getButtonPanel();

        for (String property : properties) {
            JRadioButton radioButton = new JRadioButton(property);
            radioButton.setSelected(selectedProperty.trim().equalsIgnoreCase(property.trim()));

            radioButton.addActionListener(e -> {
                selectedProperty = property;
                filter.setCurrentSelectedDb(selectedProperty);
                filter.filterView();
                stateChanged(new ChangeEvent(this));
            });

            buttonGroup.add(radioButton);
            buttonPanel.add(radioButton);
        }

        content.add(buttonPanel, layoutHelper.down().expandHoriz());
    }


    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        buttonGroup.getElements().asIterator().forEachRemaining(buttonPanel::add);

        buttonPanel.setBackground(new Color(251, 251, 251));

        return buttonPanel;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        manager.data.getCurrentNetwork().expandGroups();
        manager.data.getCurrentNetwork().collapseGroups(filter.getProperty(), selectedProperty);
    }

    @Override
    protected void updateFilterUI(RadioButtonFilter<T> filter) {
    }
}
