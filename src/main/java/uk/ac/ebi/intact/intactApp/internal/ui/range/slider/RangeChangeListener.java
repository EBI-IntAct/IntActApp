package uk.ac.ebi.intact.intactApp.internal.ui.range.slider;

import java.util.EventListener;

public interface RangeChangeListener extends EventListener {
    void rangeChanged(RangeChangeEvent event);
}
