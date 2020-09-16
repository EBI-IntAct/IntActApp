package uk.ac.ebi.intact.app.internal.model.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

import java.lang.ref.WeakReference;
import java.util.Objects;

public abstract class Filter<T extends Element> {
    public final transient Manager manager;
    private final transient WeakReference<Network> network;
    private final transient WeakReference<NetworkView> view;
    public final String name;
    public final Class<T> elementType;

    public Filter(NetworkView view, String name, Class<T> elementType) {
        this.view = new WeakReference<>(view);
        network = new WeakReference<>(view.getNetwork());
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
        NetworkView view = getNetworkView();
        if (elementType == SummaryEdge.class && view.getType() != NetworkView.Type.SUMMARY) return false;
        if (elementType == EvidenceEdge.class && view.getType() == NetworkView.Type.SUMMARY) return false;
        return true;
    }

    public Network getNetwork() {return Objects.requireNonNull(network.get());}
    public NetworkView getNetworkView() {return Objects.requireNonNull(view.get());}

}
