package uk.ac.ebi.intact.app.internal.model.filters;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.IntactNode;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.app.internal.model.core.IntactElement;

public abstract class BooleanFilter<T extends IntactElement> extends Filter<T> {
    protected boolean status = false;
    public final String description;

    public BooleanFilter(IntactNetworkView iView, Class<T> elementType, String name, String description) {
        super(iView, name, elementType);
        this.description = description;
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

        if (IntactNode.class.isAssignableFrom(elementType)) {
            iView.visibleNodes.removeIf(node -> isToHide(elementType.cast(node)));
        } else if (IntactEdge.class.isAssignableFrom(elementType)) {
            iView.visibleEdges.removeIf(edge -> isToHide(elementType.cast(edge)));
        }
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
        iView.filter();
    }
}
