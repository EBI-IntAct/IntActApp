package uk.ac.ebi.intact.app.internal.model.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.events.FilterUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

import java.util.Objects;

public abstract class Filter<T extends Element> {
    public final transient Manager manager;
    private final transient Network network;
    private final transient NetworkView view;
    public final String name;
    public final String definition;
    public final Class<T> elementType;

    public Filter(NetworkView view, String name, String definition, Class<T> elementType) {
        this.view = view;
        network = view.getNetwork();
        manager = view.manager;
        this.name = name;
        this.definition = definition;
        this.elementType = elementType;
    }

    public abstract void filterView();
    public abstract void reset();

    public boolean load(JsonNode json) {
        if (!name.equals(json.get("name").textValue())) return false;
        if (!elementType.getName().equals(json.get("elementType").textValue())) return false;
        return true;
    }

    @JsonIgnore
    public boolean isEnabled() {
        NetworkView view = getNetworkView();
        if (elementType == SummaryEdge.class && view.getType() != NetworkView.Type.SUMMARY) return false;
        if (elementType == EvidenceEdge.class && view.getType() == NetworkView.Type.SUMMARY) return false;
        return true;
    }

    public Network getNetwork() {return Objects.requireNonNull(network);}
    public NetworkView getNetworkView() {return Objects.requireNonNull(view);}

    protected void fireFilterUpdated() {
        manager.utils.fireEvent(new FilterUpdatedEvent(this));
    }
}
