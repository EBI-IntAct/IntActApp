package uk.ac.ebi.intact.app.internal.ui.panels.options.fields;

import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.ui.components.buttons.ToggleSwitch;
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
        toggleSwitch.setToolTipText(option.description);
        container.add(toggleSwitch, layoutHelper.anchor("west"));
    }

    @Override
    public void addListener(Runnable listener) {
        toggleSwitch.addChangeListener((changeEvent) -> listener.run());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        silenceUpdate = true;
        option.setValue(toggleSwitch.isActivated());
        silenceUpdate = false;
    }

    @Override
    public void updateValue() {
        toggleSwitch.setActivated(option.getValue());
    }
}
