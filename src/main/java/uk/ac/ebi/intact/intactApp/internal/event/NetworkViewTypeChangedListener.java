package uk.ac.ebi.intact.intactApp.internal.event;

import org.cytoscape.event.CyListener;

public interface NetworkViewTypeChangedListener extends CyListener {

    void handleEvent(NetworkViewTypeChangedEvent e);

}
