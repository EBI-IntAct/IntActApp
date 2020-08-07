package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.CyListener;

public interface RangeChangeListener extends CyListener {
    void handleRangeChanged(RangeChangeEvent event);
}
