package uk.ac.ebi.intact.app.internal.model.filters.edge;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryParams;

public class EdgePositiveFilter extends BooleanFilter<Edge> {

    @Setter
    @Getter
    private boolean isNegativeHidden = areTherePositiveInteractions() && areThereNegativeInteractions();
    @Setter
    @Getter
    private boolean isPositiveHidden = !areTherePositiveInteractions();

    public EdgePositiveFilter(NetworkView networkView) {
        super(networkView,
                Edge.class,
                "Positive Interaction",
                "Positive and/or negative interactions only",
                "Positive and/or negative interactions only",
                null);
        if (networkView.getNetwork().getQueryParams() != null && networkView.getNetwork().getQueryParams().getNegativeFilter() != null) {
            isNegativeHidden = QueryParams.NegativeFilterStatus.POSITIVE_ONLY.equals(
                    networkView.getNetwork().getQueryParams().getNegativeFilter());
            isPositiveHidden = QueryParams.NegativeFilterStatus.NEGATIVE_ONLY.equals(
                    networkView.getNetwork().getQueryParams().getNegativeFilter());
        } else {
            isNegativeHidden = areTherePositiveInteractions() && areThereNegativeInteractions();
            isPositiveHidden = !areTherePositiveInteractions();
        }
    }

    @Override
    public boolean isToHide(Edge element) {
        if (element.isNegative){
            return isNegativeHidden;
        } else {
            return isPositiveHidden;
        }
    }

    @Override
    public void reset() {
        boolean anyPositive = areTherePositiveInteractions();
        boolean anyNegative = areThereNegativeInteractions();
        setNegativeHidden(anyPositive && anyNegative);
        setPositiveHidden(!anyPositive);
        status = anyPositive || anyNegative;
        fireFilterUpdated();
    }

    public boolean areThereNegativeInteractions() {
        NetworkView networkView = getNetworkView();
        return networkView.getNetwork().getEvidenceEdges().stream().anyMatch(edge -> edge.isNegative);
    }

    public boolean areTherePositiveInteractions() {
        NetworkView networkView = getNetworkView();
        return networkView.getNetwork().getEvidenceEdges().stream().anyMatch(edge -> !edge.isNegative);
    }
}
