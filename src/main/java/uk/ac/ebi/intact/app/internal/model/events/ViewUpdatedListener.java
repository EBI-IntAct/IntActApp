package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.CyListener;

public interface ViewUpdatedListener extends CyListener {
    void handleEvent(ViewUpdatedEvent event);
}
