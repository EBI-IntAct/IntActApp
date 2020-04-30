package uk.ac.ebi.intact.intactApp.internal.ui.components.slider;

import java.util.EventListener;

public interface RangeChangeListener extends EventListener {
    void rangeChanged(RangeChangeEvent event);
}
