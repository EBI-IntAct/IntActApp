package uk.ac.ebi.intact.app.internal.ui.panels.options.fields;

import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.IntactOptionManager;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;

public abstract class OptionField<O extends IntactOptionManager.Option<?>> {
    protected final JPanel container;
    protected final EasyGBC layoutHelper;
    protected final O option;

    public OptionField(O option, JPanel container, EasyGBC layoutHelper) {
        this.container = container;
        this.layoutHelper = layoutHelper;
        this.option = option;
        container.add(new JLabel(option.label), layoutHelper.down().anchor("east"));
        layoutHelper.right().anchor("west").expandHoriz();
    }

    public static OptionField<?> createOptionField(IntactOptionManager.Option<?> option, JPanel container, EasyGBC layoutHelper) {
        if (option.type == Integer.class)
            return new IntegerOptionField((IntactOptionManager.NumericOption<Integer>) option, container, layoutHelper);
        if (option.type == Double.class)
            return new DoubleOptionField((IntactOptionManager.NumericOption<Double>) option, container, layoutHelper);
        if (option.type == Boolean.class)
            return new BooleanOptionField((IntactOptionManager.Option<Boolean>) option, container, layoutHelper);
        if (option.type == String.class)
            return new StringOptionField((IntactOptionManager.Option<String>) option, container, layoutHelper);
        return null;
    }
}
