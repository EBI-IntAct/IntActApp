package uk.ac.ebi.intact.intactApp.internal.ui.panels.options.fields;

import uk.ac.ebi.intact.intactApp.internal.model.managers.sub.managers.IntactOptionManager.Option;
import uk.ac.ebi.intact.intactApp.internal.ui.components.ToggleSwitch;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BooleanOptionField extends OptionField<Option<Boolean>> implements ChangeListener {

    private final ToggleSwitch toggleSwitch = new ToggleSwitch(false);

    public BooleanOptionField(Option<Boolean> option, JPanel container, EasyGBC layoutHelper) {
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
