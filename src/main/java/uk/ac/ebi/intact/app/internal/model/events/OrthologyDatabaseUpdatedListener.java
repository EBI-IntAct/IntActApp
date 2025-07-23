package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.CyListener;

public interface OrthologyDatabaseUpdatedListener extends CyListener {
    void handleEvent(OrthologyDatabaseUpdatedEvent event);
}
