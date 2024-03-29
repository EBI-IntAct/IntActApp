package uk.ac.ebi.intact.app.internal.ui.panels.options.fields;

import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;

public abstract class OptionField<O extends OptionManager.Option<?>> {
    protected final JPanel container;
    protected final EasyGBC layoutHelper;
    protected final O option;
    protected boolean silenceUpdate = false;

    public OptionField(O option, JPanel container, EasyGBC layoutHelper) {
        this.container = container;
        this.layoutHelper = layoutHelper;
        this.option = option;
        JLabel title = new JLabel(option.title);
        title.setToolTipText(option.description);
        container.add(title, layoutHelper.down().anchor("east"));
        layoutHelper.right().anchor("west").expandHoriz();
    }

    public static OptionField<?> createOptionField(OptionManager.Option<?> option, JPanel container, EasyGBC layoutHelper) {
        if (option.type == Integer.class)
            return new IntegerOptionField((OptionManager.NumericOption<Integer>) option, container, layoutHelper);
        if (option.type == Double.class)
            return new DoubleOptionField((OptionManager.NumericOption<Double>) option, container, layoutHelper);
        if (option.type == Boolean.class)
            return new BooleanOptionField((OptionManager.Option<Boolean>) option, container, layoutHelper);
        if (option.type == String.class)
            return new StringOptionField((OptionManager.Option<String>) option, container, layoutHelper);
        return null;
    }

    public abstract void addListener(Runnable listener);
    protected abstract void updateValue();

    public void update() {
        if (!silenceUpdate) updateValue();
    }
}
