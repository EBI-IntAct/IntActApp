package uk.ac.ebi.intact.intactApp.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;

public class IntactViewChangedEvent extends AbstractCyEvent<IntactManager> {
    public final IntactNetworkView.Type newType;
    public final IntactNetworkView view;

    public IntactViewChangedEvent(IntactManager source, IntactNetworkView view) {
        super(source, IntactViewTypeChangedListener.class);
        this.newType = view.getType();
        this.view = view;
    }
}
