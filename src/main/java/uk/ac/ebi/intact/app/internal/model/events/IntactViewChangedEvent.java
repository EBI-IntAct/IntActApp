package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;

public class IntactViewChangedEvent extends AbstractCyEvent<IntactManager> {
    public final IntactNetworkView.Type newType;
    public final IntactNetworkView view;

    public IntactViewChangedEvent(IntactManager source, IntactNetworkView view) {
        super(source, IntactViewTypeChangedListener.class);
        this.newType = view.getType();
        this.view = view;
    }
}
