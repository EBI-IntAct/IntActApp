package uk.ac.ebi.intact.intactApp.internal.ui.panels.options.fields;

import uk.ac.ebi.intact.intactApp.internal.model.managers.sub.managers.IntactOptionManager.NumericOption;
import uk.ac.ebi.intact.intactApp.internal.model.managers.sub.managers.IntactOptionManager.Option;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;

public abstract class OptionField<O extends Option<?>> {
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

    public static OptionField<?> createOptionField(Option<?> option, JPanel container, EasyGBC layoutHelper) {
        if (option.type == Integer.class)
            return new IntegerOptionField((NumericOption<Integer>) option, container, layoutHelper);
        if (option.type == Double.class)
            return new DoubleOptionField((NumericOption<Double>) option, container, layoutHelper);
        if (option.type == Boolean.class)
            return new BooleanOptionField((Option<Boolean>) option, container, layoutHelper);
        if (option.type == String.class)
            return new StringOptionField((Option<String>) option, container, layoutHelper);
        return null;
    }
}
