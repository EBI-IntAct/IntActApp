package uk.ac.ebi.intact.intactApp.internal.event;

import org.cytoscape.event.AbstractCyEvent;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactViewType;

public class NetworkViewTypeChangedEvent extends AbstractCyEvent<IntactManager> {

    private final CyNetworkView view;
    private final IntactViewType oldViewType;
    private final IntactViewType newViewType;

    public NetworkViewTypeChangedEvent(IntactManager source, CyNetworkView view, IntactViewType oldViewType, IntactViewType newViewType) {
        super(source, NetworkViewTypeChangedListener.class);
        this.view = view;
        this.oldViewType = oldViewType;
        this.newViewType = newViewType;
    }

    public CyNetworkView getView() {
        return view;
    }

    public IntactViewType getOldViewType() {
        return oldViewType;
    }

    public IntactViewType getNewViewType() {
        return newViewType;
    }
}
