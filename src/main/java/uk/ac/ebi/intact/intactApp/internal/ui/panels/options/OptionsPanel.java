package uk.ac.ebi.intact.intactApp.internal.ui.panels.options;

import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.managers.sub.managers.IntactOptionManager;
import uk.ac.ebi.intact.intactApp.internal.model.managers.sub.managers.IntactOptionManager.Option;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.options.fields.OptionField;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;

public class OptionsPanel extends JPanel {

    public OptionsPanel(IntactManager manager) {
        super(new GridBagLayout());
        EasyGBC layoutHelper = new EasyGBC().insets(2,2,2,2);
        for (Option<?> option : manager.option.options) {
            OptionField.createOptionField(option, this, layoutHelper);
        }
    }

    public OptionsPanel(IntactManager manager, IntactOptionManager.Scope scope) {
        super(new GridBagLayout());
        EasyGBC layoutHelper = new EasyGBC().insets(2,2,2,2);
        for (Option<?> option : manager.option.scopeOptions.get(scope)) {
            OptionField.createOptionField(option, this, layoutHelper);
        }
    }
}
