package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;

public class IntactNetworkCreatedEvent extends AbstractCyEvent<Manager> {
    private final Network newINetwork;

    public IntactNetworkCreatedEvent(Manager source, Network newINetwork) {
        super(source, IntactNetworkCreatedListener.class);
        this.newINetwork = newINetwork;
    }

    public Network getNewINetwork() {
        return newINetwork;
    }
}
