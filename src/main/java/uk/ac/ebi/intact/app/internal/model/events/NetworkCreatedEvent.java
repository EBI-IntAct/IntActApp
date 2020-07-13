package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;

public class NetworkCreatedEvent extends AbstractCyEvent<Manager> {
    private final Network newNetwork;

    public NetworkCreatedEvent(Manager source, Network newNetwork) {
        super(source, NetworkCreatedListener.class);
        this.newNetwork = newNetwork;
    }

    public Network getNewNetwork() {
        return newNetwork;
    }
}
