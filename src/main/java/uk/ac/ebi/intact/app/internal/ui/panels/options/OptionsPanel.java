package uk.ac.ebi.intact.app.internal.ui.panels.options;

import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.IntactOptionManager;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.app.internal.ui.panels.options.fields.OptionField;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;

public class OptionsPanel extends JPanel {

    public OptionsPanel(IntactManager manager) {
        super(new GridBagLayout());
        EasyGBC layoutHelper = new EasyGBC().insets(2,2,2,2);
        for (IntactOptionManager.Option<?> option : manager.option.options) {
            OptionField.createOptionField(option, this, layoutHelper);
        }
    }

    public OptionsPanel(IntactManager manager, IntactOptionManager.Scope scope) {
        super(new GridBagLayout());
        EasyGBC layoutHelper = new EasyGBC().insets(2,2,2,2);
        for (IntactOptionManager.Option<?> option : manager.option.scopeOptions.get(scope)) {
            OptionField.createOptionField(option, this, layoutHelper);
        }
    }
}
