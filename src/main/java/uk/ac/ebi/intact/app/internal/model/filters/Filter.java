package uk.ac.ebi.intact.app.internal.model.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.model.IntactNetwork;
import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.IntactElement;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;

public abstract class Filter<T extends IntactElement> {
    public final transient IntactManager manager;
    public final transient IntactNetwork iNetwork;
    public final transient IntactNetworkView iView;
    public final String name;
    public final Class<T> elementType;

    public Filter(IntactNetworkView iView, String name, Class<T> elementType) {
        this.iView = iView;
        iNetwork = iView.network;
        manager = iView.manager;
        this.name = name;
        this.elementType = elementType;
    }

    public abstract void filterView();

    public boolean load(JsonNode json) {
        if (!name.equals(json.get("name").textValue())) return false;
        if (!elementType.getName().equals(json.get("elementType").textValue())) return false;
        return true;
    }

    @JsonIgnore
    public boolean isEnabled() {
        if (elementType == IntactCollapsedEdge.class && iView.getType() != IntactNetworkView.Type.COLLAPSED) return false;
        if (elementType == IntactEvidenceEdge.class && iView.getType() == IntactNetworkView.Type.COLLAPSED) return false;
        return true;
    }

}
