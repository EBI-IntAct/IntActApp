package uk.ac.ebi.intact.intactApp.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;

public class IntactViewTypeChangedEvent extends AbstractCyEvent<IntactManager> {
    public final IntactNetworkView.Type newType;

    public IntactViewTypeChangedEvent(IntactManager source, IntactNetworkView.Type newType) {
        super(source, IntactViewTypeChangedListener.class);
        this.newType = newType;
    }
}
