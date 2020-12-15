package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.filters.ContinuousFilter;

public class EdgeNumberSummarizedFilter extends ContinuousFilter<SummaryEdge> {

    public EdgeNumberSummarizedFilter(NetworkView view) {
        super(view, SummaryEdge.class, "# Summarized edges", "Number of evidence of interaction found between two molecules");
    }

    @Override
    public double getProperty(SummaryEdge element) {
        return element.getNbSummarizedEdges();
    }
}
