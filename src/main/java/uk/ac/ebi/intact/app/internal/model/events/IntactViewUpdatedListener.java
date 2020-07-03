package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.CyListener;

public interface IntactViewUpdatedListener extends CyListener {
    void handleEvent(IntactViewUpdatedEvent event);
}
