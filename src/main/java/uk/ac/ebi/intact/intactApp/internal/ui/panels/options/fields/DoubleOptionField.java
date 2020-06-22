package uk.ac.ebi.intact.intactApp.internal.ui.panels.options.fields;

import uk.ac.ebi.intact.intactApp.internal.model.managers.sub.managers.IntactOptionManager.NumericOption;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;

public class DoubleOptionField extends OptionField<NumericOption<Double>> {
    //TODO: Double option field: look at Integer option field and at confidence in String App
    public DoubleOptionField(NumericOption<Double> option, JPanel container, EasyGBC layoutHelper) {
        super(option, container, layoutHelper);
    }

}
