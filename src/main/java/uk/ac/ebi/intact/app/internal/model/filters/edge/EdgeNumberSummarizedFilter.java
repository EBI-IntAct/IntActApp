package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.filters.ContinuousFilter;

public class EdgeNumberSummarizedFilter extends ContinuousFilter<SummaryEdge> {

    public EdgeNumberSummarizedFilter(NetworkView view) {
        super(view, SummaryEdge.class, "# Summarized edges");
    }

    @Override
    public double getProperty(SummaryEdge element) {
        return element.summarizedEdgeSUIDs.size();
    }
}
