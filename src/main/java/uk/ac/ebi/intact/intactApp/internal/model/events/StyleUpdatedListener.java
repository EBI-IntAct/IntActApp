package uk.ac.ebi.intact.intactApp.internal.model.events;

import org.cytoscape.event.CyListener;

public interface StyleUpdatedListener extends CyListener {
    void handleStyleUpdatedEvent();
}
