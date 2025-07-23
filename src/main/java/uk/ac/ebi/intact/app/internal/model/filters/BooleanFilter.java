package uk.ac.ebi.intact.app.internal.model.filters;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;

public abstract class BooleanFilter<T extends Element> extends Filter<T> {
    protected boolean status;
    public final String description;

    public BooleanFilter(NetworkView view, Class<T> elementType, String name, String definition, String description, Boolean status) {
        super(view, name, definition, elementType);
        this.description = description;
        this.status = status != null ? status : false;
    }

    public abstract boolean isToHide(T element);

    @Override
    public boolean load(JsonNode json) {
        if (!super.load(json)) return false;
        if (!description.equals(json.get("description").textValue())) return false;
        status = json.get("status").booleanValue();
        return true;
    }

    @Override
    public void filterView() {
        if (!isEnabled() || !status) return;
        NetworkView view = getNetworkView();
        if (Node.class.isAssignableFrom(elementType)) {
            view.getNetwork().getVisibleNodes().removeIf(node -> isToHide(elementType.cast(node)));
        } else if (Edge.class.isAssignableFrom(elementType)) {
            view.getNetwork().getVisibleEvidenceEdges().removeIf(edge -> isToHide(elementType.cast(edge)));
            view.getNetwork().getVisibleSummaryEdges().removeIf(edge -> isToHide(elementType.cast(edge)));
        }
    }

    @Override
    public void reset() {
        status = false;
        fireFilterUpdated();
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
        fireFilterUpdated();
    }
}
