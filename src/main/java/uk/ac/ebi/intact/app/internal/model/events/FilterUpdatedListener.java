package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.CyListener;

public interface FilterUpdatedListener extends CyListener {
    void handleEvent(FilterUpdatedEvent event);
}
