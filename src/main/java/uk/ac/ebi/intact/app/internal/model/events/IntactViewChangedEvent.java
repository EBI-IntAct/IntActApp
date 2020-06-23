package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;

public class IntactViewChangedEvent extends AbstractCyEvent<Manager> {
    public final NetworkView.Type newType;
    public final NetworkView view;

    public IntactViewChangedEvent(Manager source, NetworkView view) {
        super(source, IntactViewTypeChangedListener.class);
        this.newType = view.getType();
        this.view = view;
    }
}
