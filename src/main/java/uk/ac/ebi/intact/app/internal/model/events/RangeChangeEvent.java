package uk.ac.ebi.intact.app.internal.model.events;

import uk.ac.ebi.intact.app.internal.ui.components.slider.RangeSlider;

import java.util.EventObject;

public class RangeChangeEvent extends EventObject {
    private final RangeSlider rangeSlider;

    public RangeChangeEvent(RangeSlider source) {
        super(source);
        rangeSlider = source;
    }

    public RangeSlider getRangeSlider() {
        return rangeSlider;
    }
}
