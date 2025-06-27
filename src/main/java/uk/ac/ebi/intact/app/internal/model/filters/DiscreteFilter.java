package uk.ac.ebi.intact.app.internal.model.filters;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;

import java.util.*;

public abstract class DiscreteFilter<T extends Element> extends Filter<T> {
    protected final Map<String, Boolean> propertiesVisibility = new HashMap<>();

    public DiscreteFilter(NetworkView view, Class<T> elementType, String name, String definition) {
        this(view, elementType, name, definition, null);
    }

    public DiscreteFilter(NetworkView view, Class<T> elementType, String name, String definition, Set<String> selectedProperties) {
        super(view, name, definition, elementType);
        List<T> elements;
        Network network = getNetwork();
        if (elementType == Node.class) {
            elements = new ArrayList<>((List<T>) network.getNodes());
        } else if (elementType == Edge.class) {
            elements = new ArrayList<>((List<T>) network.getSummaryEdges());
            elements.addAll((List<T>) network.getEvidenceEdges());
        } else if (elementType == SummaryEdge.class) {
            elements = new ArrayList<>((List<T>) network.getSummaryEdges());
        } else if (elementType == EvidenceEdge.class) {
            elements = new ArrayList<>((List<T>) network.getEvidenceEdges());
        } else return;

        for (T element : elements) {
            if (element != null) {
                String property = getProperty(element);
                propertiesVisibility.put(property, selectedProperties == null || selectedProperties.contains(property));
            }
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
            NetworkView view = getNetworkView();
            if (Node.class.isAssignableFrom(elementType)) {
                view.visibleNodes.removeIf(node -> !propertiesVisibility.get(getProperty(elementType.cast(node))));
            } else if (Edge.class.isAssignableFrom(elementType)) {
                if (elementType == SummaryEdge.class && view.getType() != NetworkView.Type.SUMMARY) return;
                if (elementType == EvidenceEdge.class && view.getType() == NetworkView.Type.SUMMARY) return;
                view.visibleEdges.removeIf(edge -> !propertiesVisibility.get(getProperty(elementType.cast(edge))));
            }
        }
    }

    @Override
    public void reset() {
        propertiesVisibility.replaceAll((k, v) -> v = true);
        fireFilterUpdated();
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
            getNetworkView().filter();
            fireFilterUpdated();
        }
    }

    public Map<String, Boolean> getPropertiesVisibility() {
        return new HashMap<>(propertiesVisibility);
    }

    public Set<String> getProperties() {
        return new HashSet<>(propertiesVisibility.keySet());
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && propertiesVisibility.size() > 1;
    }
}
