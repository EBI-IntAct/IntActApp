package uk.ac.ebi.intact.app.internal.ui.panels.options;

import uk.ac.ebi.intact.app.internal.model.events.OptionUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.OptionUpdatedListener;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.ui.panels.options.fields.OptionField;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class OptionsPanel extends JPanel implements OptionUpdatedListener {

    private final Map<OptionManager.Option, OptionField> fields = new HashMap<>();

    public OptionsPanel(Manager manager) {
        this(manager, manager.option.options);
    }

    public OptionsPanel(Manager manager, OptionManager.Scope scope) {
        this(manager, manager.option.scopeOptions.get(scope));
    }

    private OptionsPanel(Manager manager, Collection<OptionManager.Option<?>> options) {
        super(new GridBagLayout());
        manager.utils.registerAllServices(this, new Properties());
        EasyGBC layoutHelper = new EasyGBC().insets(2, 2, 2, 2);
        for (OptionManager.Option<?> option : options) {
            fields.put(option, OptionField.createOptionField(option, this, layoutHelper.noExpand()));
            add(Box.createHorizontalGlue(), layoutHelper.right().expandHoriz());
        }
    }

    public void addListener(OptionManager.Option<?> option, Runnable listener) {
        OptionField<?> optionField = fields.get(option);
        if (optionField != null) optionField.addListener(listener);
    }

    @Override
    public void handleEvent(OptionUpdatedEvent event) {
        fields.get(event.getSource()).update();
    }
}
