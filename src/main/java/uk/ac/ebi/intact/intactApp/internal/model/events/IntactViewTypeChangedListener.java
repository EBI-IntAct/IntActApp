package uk.ac.ebi.intact.intactApp.internal.model.events;

import org.cytoscape.event.CyListener;

public interface IntactViewTypeChangedListener extends CyListener {
    void handleEvent(IntactViewTypeChangedEvent event);
}
