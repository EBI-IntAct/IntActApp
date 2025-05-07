package uk.ac.ebi.intact.app.internal.model.styles;

import org.cytoscape.view.vizmap.VisualStyle;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public class OrthologyStyle extends Style{
    public final static NetworkView.Type type = NetworkView.Type.ORTHOLOGY;

    public OrthologyStyle(Manager manager, VisualStyle style) {
        super(manager, style);
    }

    public OrthologyStyle(Manager manager) {
        super(manager);
    }

    @Override
    protected void setEdgePaintStyle() {

    }

    @Override
    public NetworkView.Type getStyleViewType() {
        return type;
    }
}
