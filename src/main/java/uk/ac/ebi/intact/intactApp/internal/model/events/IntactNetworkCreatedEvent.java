package uk.ac.ebi.intact.intactApp.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;

public class IntactNetworkCreatedEvent extends AbstractCyEvent<IntactManager> {
    private final IntactNetwork newINetwork;

    public IntactNetworkCreatedEvent(IntactManager source, IntactNetwork newINetwork) {
        super(source, IntactNetworkCreatedListener.class);
        this.newINetwork = newINetwork;
    }

    public IntactNetwork getNewINetwork() {
        return newINetwork;
    }
}
