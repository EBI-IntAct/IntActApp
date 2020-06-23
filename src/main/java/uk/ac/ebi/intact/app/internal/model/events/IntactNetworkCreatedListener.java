package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.CyListener;

public interface IntactNetworkCreatedListener extends CyListener {
    void handleEvent(IntactNetworkCreatedEvent event);
}
