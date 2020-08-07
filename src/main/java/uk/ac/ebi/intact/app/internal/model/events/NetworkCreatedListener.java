package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.CyListener;

public interface NetworkCreatedListener extends CyListener {
    void handleEvent(NetworkCreatedEvent event);
}
