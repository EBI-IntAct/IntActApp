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
    private final Map<String, Boolean> propertiesVisibility = new HashMap<>();
    private final Map<String, String> propertyLabels = new HashMap<>();
    private final Set<String> propertiesSet = new HashSet<>();
    private final Set<String> propertyLabelsSet = new HashSet<>();

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
                Map<String, String> properties = getProperties(element);
                properties.forEach((propertyId, propertyValue) -> {
                    propertiesVisibility.put(propertyId, selectedProperties == null || selectedProperties.contains(propertyId));
                    propertyLabels.put(propertyId, propertyValue);
                    propertiesSet.add(propertyId);
                    propertyLabelsSet.add(propertyValue);
                });
            }
        }
    }

    @Override
    public boolean load(JsonNode json) {
        if (!super.load(json)) return false;
        // When loading sessions from older versions of IntAct App, these properties are not set, so we cannot load them.
        if (json.has("propertiesVisibility")) {
            propertiesVisibility.clear();
            propertiesSet.clear();
            json.get("propertiesVisibility").fields().forEachRemaining(entry -> {
                propertiesVisibility.put(entry.getKey(), entry.getValue().booleanValue());
                propertiesSet.add(entry.getKey());
            });
            propertiesSet.addAll(propertiesVisibility.keySet());
        }
        if (json.has("propertyLabels")) {
            propertyLabels.clear();
            propertyLabelsSet.clear();
            json.get("propertyLabels").fields().forEachRemaining(entry -> {
                propertyLabels.put(entry.getKey(), entry.getValue().textValue());
                propertyLabelsSet.add(entry.getValue().textValue());
            });
        }
        return true;
    }

    protected abstract Map<String, String> getPropertyValues(T element);

    public Map<String, String> getProperties(T element) {
        Map<String, String> properties = getPropertyValues(element);
        return properties != null ? properties : Map.of();
    }

    @Override
    public void filterView() {
        if (propertiesVisibility.values().stream().anyMatch(visible -> !visible)) {
            NetworkView view = getNetworkView();
            if (Node.class.isAssignableFrom(elementType)) {
                view.getNetwork().getVisibleNodes().removeIf(node ->
                        getProperties(elementType.cast(node)).keySet().stream().noneMatch(propertiesVisibility::get));
            } else if (Edge.class.isAssignableFrom(elementType)) {
                if (elementType == SummaryEdge.class && view.getType() != NetworkView.Type.SUMMARY) return;
                if (elementType == EvidenceEdge.class && view.getType() == NetworkView.Type.SUMMARY) return;
                view.getNetwork().getVisibleEvidenceEdges().removeIf(edge ->
                        getProperties(elementType.cast(edge)).keySet().stream().noneMatch(propertiesVisibility::get));
                view.getNetwork().getVisibleSummaryEdges().removeIf(edge ->
                        getProperties(elementType.cast(edge)).keySet().stream().noneMatch(propertiesVisibility::get));
            }
        }
    }

    @Override
    public void reset() {
        propertiesVisibility.replaceAll((k, v) -> v = true);
        fireFilterUpdated();
    }

    public boolean getPropertyVisibility(String propertyValueToString) {
        return propertiesVisibility.getOrDefault(propertyValueToString, false);
    }

    public String getPropertyLabel(String propertyValueToString) {
        return propertyLabels.getOrDefault(propertyValueToString, "");
    }

    public void setPropertyVisibility(String propertyValueToString, boolean visible) {
        if (propertiesVisibility.containsKey(propertyValueToString) && propertiesVisibility.get(propertyValueToString) != visible) {
            propertiesVisibility.put(propertyValueToString, visible);
            getNetworkView().filter();
            fireFilterUpdated();
        }
    }

    public Set<String> getProperties() {
        return propertiesSet;
    }

    public Set<String> getPropertiesLabels() {
        return propertyLabelsSet;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && propertiesVisibility.size() > 1;
    }
}
