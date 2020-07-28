package uk.ac.ebi.intact.app.internal.tasks.clone;

import org.cytoscape.event.CyListener;

public interface TableClonedListener extends CyListener {
    void handleEvent(TableClonedEvent e);
}
