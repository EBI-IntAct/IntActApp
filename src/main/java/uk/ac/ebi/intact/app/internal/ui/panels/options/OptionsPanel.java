package uk.ac.ebi.intact.app.internal.ui.panels.options;

import uk.ac.ebi.intact.app.internal.model.core.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.panels.options.fields.OptionField;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;

public class OptionsPanel extends JPanel {

    public OptionsPanel(Manager manager) {
        super(new GridBagLayout());
        EasyGBC layoutHelper = new EasyGBC().insets(2,2,2,2);
        for (OptionManager.Option<?> option : manager.option.options) {
            OptionField.createOptionField(option, this, layoutHelper);
        }
    }

    public OptionsPanel(Manager manager, OptionManager.Scope scope) {
        super(new GridBagLayout());
        EasyGBC layoutHelper = new EasyGBC().insets(2,2,2,2);
        for (OptionManager.Option<?> option : manager.option.scopeOptions.get(scope)) {
            OptionField.createOptionField(option, this, layoutHelper);
        }
    }
}
