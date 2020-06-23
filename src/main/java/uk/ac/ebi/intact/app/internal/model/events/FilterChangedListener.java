package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.CyListener;

public interface FilterChangedListener extends CyListener {
    void handleEvent(FilterChangedEvent event);
}
