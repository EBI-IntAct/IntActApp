package uk.ac.ebi.intact.intactApp.internal.model.filters;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactElement;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public abstract class ContinuousFilter<T extends IntactElement> extends Filter<T> {
    protected double min;
    protected double max;
    protected double currentMin;
    protected double currentMax;

    public ContinuousFilter(IntactNetworkView iView, Class<T> elementType, String name, double min, double max) {
        super(iView, name, elementType);
        this.min = min;
        this.max = max;
        currentMin = min;
        currentMax = max;
    }

    public ContinuousFilter(IntactNetworkView iView, Class<T> elementType, String name) {
        super(iView, name, elementType);
        List<? extends IntactElement> elements;
        if (IntactNode.class.isAssignableFrom(elementType)) {
            elements = iNetwork.getINodes();
        } else if (elementType == IntactCollapsedEdge.class) {
            elements = iNetwork.getCollapsedIEdges();
        } else if (elementType == IntactEvidenceEdge.class) {
            elements = iNetwork.getEvidenceIEdges();
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
        Collection<? extends IntactElement> elementsToFilter;

        if (IntactNode.class.isAssignableFrom(elementType)) {
            elementsToFilter = iView.visibleNodes;
        } else if (IntactEdge.class.isAssignableFrom(elementType)) {
            if (elementType == IntactCollapsedEdge.class && iView.getType() != IntactNetworkView.Type.COLLAPSED) return;
            if (elementType == IntactEvidenceEdge.class && iView.getType() == IntactNetworkView.Type.COLLAPSED) return;
            elementsToFilter = iView.visibleEdges;
        } else {
            return;
        }

        elementsToFilter.removeIf(element -> {
            double property = getProperty(elementType.cast(element));
            return property < currentMin || property > currentMax;
        });
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
        iView.filter();
    }

    public double getCurrentMax() {
        return currentMax;
    }

    public void setCurrentMax(double currentMax) {
        this.currentMax = currentMax;
        iView.filter();
    }

    public void setCurrentPositions(double min, double max) {
        this.currentMin = min;
        this.currentMax = max;
        iView.filter();
    }
}
