package uk.ac.ebi.intact.app.internal.ui.panels.filters;

import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;
import uk.ac.ebi.intact.app.internal.model.filters.edge.EdgePositiveFilter;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ToggleFilterPanel<T extends Element> extends FilterPanel<BooleanFilter<T>> implements ChangeListener {
    private final ButtonGroup group = new ButtonGroup();
    private final JToggleButton positiveButton = new JToggleButton("✔");
    private final JToggleButton negativeButton = new JToggleButton("❌");
    private final JToggleButton bothButton = new JToggleButton("✔/❌");

    private final JLabel label = new JLabel("");

    public ToggleFilterPanel(Manager manager, BooleanFilter<T> filter) {
        super(manager, filter);
        buildButtons();

        positiveButton.addChangeListener(this);
        negativeButton.addChangeListener(this);
        bothButton.addChangeListener(this);

        updateFilterUI(filter);
    }

    private void buildButtons() {
        group.add(positiveButton);
        group.add(negativeButton);
        group.add(bothButton);
        setButtonsEnabled();

        JPanel buttonPanel = getButtonPanel();
        content.add(buttonPanel, layoutHelper.down().expandHoriz());

        positiveButton.addActionListener(e -> updateFilter(true, false));
        negativeButton.addActionListener(e -> updateFilter(false, true));
        bothButton.addActionListener(e -> updateFilter(false, false));
    }

    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(positiveButton);
        buttonPanel.add(bothButton);
        buttonPanel.add(negativeButton);

        buttonPanel.setBackground(new Color(251, 251, 251));

        return buttonPanel;
    }

    private void setButtonsEnabled() {
        boolean isThereNegativeInteractions = ((EdgePositiveFilter) filter).areThereNegativeInteractions();
        boolean isTherePositiveInteractions = ((EdgePositiveFilter) filter).areTherePositiveInteractions();

        negativeButton.setEnabled(isThereNegativeInteractions);
        positiveButton.setEnabled(isTherePositiveInteractions);
        bothButton.setEnabled(isThereNegativeInteractions && isTherePositiveInteractions);
    }

    private void updateFilter(boolean showOnlyPositive, boolean showOnlyNegative) {
        ((EdgePositiveFilter) filter).setPositiveHidden(showOnlyNegative);
        ((EdgePositiveFilter) filter).setNegativeHidden(showOnlyPositive);

        boolean shouldEnableFilter = showOnlyPositive || showOnlyNegative;
        filter.setStatus(shouldEnableFilter);

        positiveButton.setSelected(showOnlyPositive && !showOnlyNegative);
        negativeButton.setSelected(!showOnlyPositive && showOnlyNegative);
        bothButton.setSelected(!showOnlyPositive && !showOnlyNegative);
    }

    @Override
    protected void updateFilterUI(BooleanFilter<T> filter) {
        label.setText(filter.description);
        setButtonsEnabled();

        boolean isThereNegativeInteractions = ((EdgePositiveFilter) filter).areThereNegativeInteractions();
        boolean isTherePositiveInteractions = ((EdgePositiveFilter) filter).areTherePositiveInteractions();

        negativeButton.setSelected(isThereNegativeInteractions && !isTherePositiveInteractions);
        positiveButton.setSelected(isTherePositiveInteractions);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        setListening(false);
        boolean shouldEnableFilter = ((EdgePositiveFilter) filter).isPositiveHidden() || ((EdgePositiveFilter) filter).isNegativeHidden();
        filter.setStatus(shouldEnableFilter);
        setListening(true);
    }

}
