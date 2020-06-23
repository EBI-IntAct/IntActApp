package uk.ac.ebi.intact.app.internal.model.filters;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.IntactNode;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.IntactElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DiscreteFilter<T extends IntactElement> extends Filter<T> {
    protected final Map<String, Boolean> propertiesVisibility = new HashMap<>();

    public DiscreteFilter(IntactNetworkView iView, Class<T> elementType, String name) {
        super(iView, name, elementType);
        List<T> elements;
        if (elementType == IntactNode.class) {
            elements = new ArrayList<>((List<T>) iNetwork.getINodes());
        } else if (elementType == IntactEdge.class) {
            elements = new ArrayList<>((List<T>) iNetwork.getCollapsedIEdges());
            elements.addAll((List<T>) iNetwork.getEvidenceIEdges());
        } else if (elementType == IntactCollapsedEdge.class) {
            elements = new ArrayList<>((List<T>) iNetwork.getCollapsedIEdges());
        } else if (elementType == IntactEvidenceEdge.class) {
            elements = new ArrayList<>((List<T>) iNetwork.getEvidenceIEdges());
        } else return;

        for (T element : elements) {
            propertiesVisibility.put(getProperty(element), true);
        }
    }

    @Override
    public boolean load(JsonNode json) {
        if (!super.load(json)) return false;
        propertiesVisibility.clear();
        json.get("propertiesVisibility").fields()
                .forEachRemaining(entry -> propertiesVisibility.put(entry.getKey(), entry.getValue().booleanValue()));
        return true;
    }

    protected abstract String getPropertyValue(T element);

    public String getProperty(T element) {
        String property = getPropertyValue(element);
        return property != null ? property : "";
    }

    @Override
    public void filterView() {
        if (propertiesVisibility.values().stream().anyMatch(visible -> !visible)) {
            if (IntactNode.class.isAssignableFrom(elementType)) {
                iView.visibleNodes.removeIf(node -> !propertiesVisibility.get(getProperty(elementType.cast(node))));
            } else if (IntactEdge.class.isAssignableFrom(elementType)) {
                if (elementType == IntactCollapsedEdge.class && iView.getType() != IntactNetworkView.Type.COLLAPSED) return;
                if (elementType == IntactEvidenceEdge.class && iView.getType() == IntactNetworkView.Type.COLLAPSED) return;
                iView.visibleEdges.removeIf(edge -> !propertiesVisibility.get(getProperty(elementType.cast(edge))));
            }
        }
    }


    public boolean getPropertyVisibility(String propertyValueToString) {
        if (propertiesVisibility.containsKey(propertyValueToString)) {
            return propertiesVisibility.get(propertyValueToString);
        }
        return false;
    }


    public void setPropertyVisibility(String propertyValueToString, boolean visible) {
        if (propertiesVisibility.containsKey(propertyValueToString) && propertiesVisibility.get(propertyValueToString) != visible) {
            propertiesVisibility.put(propertyValueToString, visible);
            iView.filter();
        }
    }

    public Map<String, Boolean> getPropertiesVisibility() {
        return new HashMap<>(propertiesVisibility);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && propertiesVisibility.size() > 1;
    }
}
