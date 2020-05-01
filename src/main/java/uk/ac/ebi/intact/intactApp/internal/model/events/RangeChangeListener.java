package uk.ac.ebi.intact.intactApp.internal.model.events;

import java.util.EventListener;

public interface RangeChangeListener extends EventListener {
    void rangeChanged(RangeChangeEvent event);
}
