package uk.ac.ebi.intact.app.internal.model.filters;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public abstract class ContinuousFilter<T extends Element> extends Filter<T> {
    protected double min;
    protected double max;
    protected double currentMin;
    protected double currentMax;

    public ContinuousFilter(NetworkView view,
                            Class<T> elementType,
                            String name,
                            String definition,
                            double min,
                            double max,
                            Double currentMin,
                            Double currentMax) {
        super(view, name, definition, elementType);
        this.min = min;
        this.max = max;
        this.currentMin = currentMin != null ? currentMin : min;
        this.currentMax = currentMax != null ? currentMax : max;
    }

    public ContinuousFilter(NetworkView view, Class<T> elementType, String name, String definition) {
        super(view, name, definition, elementType);
        List<? extends Element> elements;
        Network network = getNetwork();
        if (Node.class.isAssignableFrom(elementType)) {
            elements = network.getNodes();
        } else if (elementType == SummaryEdge.class) {
            elements = network.getSummaryEdges();
        } else if (elementType == EvidenceEdge.class) {
            elements = network.getEvidenceEdges();
        } else throw new IllegalArgumentException();

        DoubleSummaryStatistics stats = elements.stream().mapToDouble(value -> getProperty(elementType.cast(value))).summaryStatistics();

        this.min = stats.getMin();
        this.max = stats.getMax();
        currentMin = min;
        currentMax = max;
    }

    @Override
    public boolean load(JsonNode json) {
        if (!super.load(json)) return false;
        min = json.get("min").doubleValue();
        max = json.get("max").doubleValue();
        currentMin = json.get("currentMin").doubleValue();
        currentMax = json.get("currentMax").doubleValue();
        return true;
    }

    public abstract double getProperty(T element);

    @Override
    public void filterView() {
        if (currentMin == min && currentMax == max) return;
        Collection<? extends Element> elementsToFilter;
        NetworkView view = getNetworkView();
        if (Node.class.isAssignableFrom(elementType)) {
            elementsToFilter = view.visibleNodes;
        } else if (Edge.class.isAssignableFrom(elementType)) {
            if (elementType == SummaryEdge.class && view.getType() != NetworkView.Type.SUMMARY) return;
            if (elementType == EvidenceEdge.class && view.getType() == NetworkView.Type.SUMMARY) return;
            elementsToFilter = view.visibleEdges;
        } else {
            return;
        }

        elementsToFilter.removeIf(element -> {
            double property = getProperty(elementType.cast(element));
            return property < currentMin || property > currentMax;
        });
    }

    @Override
    public void reset() {
        currentMax = max;
        currentMin = min;
        fireFilterUpdated();
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        if (min < max) {
            if (min > currentMin) currentMin = min;
            this.min = min;
        }
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        if (max > min) {
            if (max > currentMax) currentMax = max;
            this.max = max;
        }
    }

    public double getCurrentMin() {
        return currentMin;
    }

    public void setCurrentMin(double currentMin) {
        this.currentMin = currentMin;
        fireFilterUpdated();
    }

    public double getCurrentMax() {
        return currentMax;
    }

    public void setCurrentMax(double currentMax) {
        this.currentMax = currentMax;
        fireFilterUpdated();
    }

    public void setCurrentPositions(double min, double max) {
        this.currentMin = min;
        this.currentMax = max;
        fireFilterUpdated();
    }
}
