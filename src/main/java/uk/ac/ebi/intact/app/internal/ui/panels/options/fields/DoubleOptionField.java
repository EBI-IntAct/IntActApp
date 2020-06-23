package uk.ac.ebi.intact.app.internal.ui.panels.options.fields;

import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.IntactOptionManager;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;

public class DoubleOptionField extends OptionField<IntactOptionManager.NumericOption<Double>> {
    //TODO: Double option field: look at Integer option field and at confidence in String App
    public DoubleOptionField(IntactOptionManager.NumericOption<Double> option, JPanel container, EasyGBC layoutHelper) {
        super(option, container, layoutHelper);
    }

}
