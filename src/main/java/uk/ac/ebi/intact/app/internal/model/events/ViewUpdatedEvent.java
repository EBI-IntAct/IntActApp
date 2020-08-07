package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public class ViewUpdatedEvent extends AbstractCyEvent<Manager> {
    public final NetworkView.Type newType;
    public final NetworkView view;

    public ViewUpdatedEvent(Manager source, NetworkView view) {
        super(source, ViewUpdatedListener.class);
        this.newType = view.getType();
        this.view = view;
    }
}
