package uk.ac.ebi.intact.app.internal.ui.panels.options;

import uk.ac.ebi.intact.app.internal.managers.Manager;
import uk.ac.ebi.intact.app.internal.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.ui.panels.options.fields.OptionField;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class OptionsPanel extends JPanel {

    private final Map<OptionManager.Option, OptionField> fields = new HashMap<>();

    public OptionsPanel(Manager manager) {
        super(new GridBagLayout());
        EasyGBC layoutHelper = new EasyGBC().insets(2, 2, 2, 2);
        for (OptionManager.Option<?> option : manager.option.options) {
            fields.put(option, OptionField.createOptionField(option, this, layoutHelper.noExpand()));
            add(Box.createHorizontalGlue(), layoutHelper.right().expandHoriz());
        }
    }

    public OptionsPanel(Manager manager, OptionManager.Scope scope) {
        super(new GridBagLayout());
        EasyGBC layoutHelper = new EasyGBC().insets(2, 2, 2, 2);
        for (OptionManager.Option<?> option : manager.option.scopeOptions.get(scope)) {
            fields.put(option, OptionField.createOptionField(option, this, layoutHelper.noExpand()));
            add(Box.createHorizontalGlue(), layoutHelper.right().expandHoriz());
        }
    }

    public void addListener(OptionManager.Option<?> option, Runnable listener) {
        OptionField<?> optionField = fields.get(option);
        if (optionField != null) {
            optionField.addListener(listener);
        }
    }
}
