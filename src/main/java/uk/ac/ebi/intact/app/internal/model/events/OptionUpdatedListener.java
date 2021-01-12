package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.CyListener;

public interface OptionUpdatedListener extends CyListener {
    void handleEvent(OptionUpdatedEvent event);
}
