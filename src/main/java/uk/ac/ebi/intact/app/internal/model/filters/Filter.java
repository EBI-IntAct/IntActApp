package uk.ac.ebi.intact.app.internal.model.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.managers.Manager;

public abstract class Filter<T extends Element> {
    public final transient Manager manager;
    public final transient Network network;
    public final transient NetworkView view;
    public final String name;
    public final Class<T> elementType;

    public Filter(NetworkView view, String name, Class<T> elementType) {
        this.view = view;
        network = view.network;
        manager = view.manager;
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
        if (elementType == SummaryEdge.class && view.getType() != NetworkView.Type.SUMMARY) return false;
        if (elementType == EvidenceEdge.class && view.getType() == NetworkView.Type.SUMMARY) return false;
        return true;
    }

}
