package uk.ac.ebi.intact.app.internal.ui.panels.options.fields;

import uk.ac.ebi.intact.app.internal.model.core.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.ui.components.ToggleSwitch;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BooleanOptionField extends OptionField<OptionManager.Option<Boolean>> implements ChangeListener {

    private final ToggleSwitch toggleSwitch = new ToggleSwitch(false);

    public BooleanOptionField(OptionManager.Option<Boolean> option, JPanel container, EasyGBC layoutHelper) {
        super(option, container, layoutHelper);
        toggleSwitch.setActivated(option.getValue());
        toggleSwitch.addChangeListener(this);
        container.add(toggleSwitch, layoutHelper);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        option.setValue(toggleSwitch.isActivated());
    }
}
