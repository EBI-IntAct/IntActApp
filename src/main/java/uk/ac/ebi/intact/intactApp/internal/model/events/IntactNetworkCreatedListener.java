package uk.ac.ebi.intact.intactApp.internal.model.events;

import org.cytoscape.event.CyListener;

public interface IntactNetworkCreatedListener extends CyListener {
    void handleEvent(IntactNetworkCreatedEvent event);
}
