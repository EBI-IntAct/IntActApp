package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.managers.Manager;

public class IntactViewUpdatedEvent extends AbstractCyEvent<Manager> {
    public final NetworkView.Type newType;
    public final NetworkView view;

    public IntactViewUpdatedEvent(Manager source, NetworkView view) {
        super(source, IntactViewUpdatedListener.class);
        this.newType = view.getType();
        this.view = view;
    }
}
