package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.filters.ContinuousFilter;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryFilters;

public class EdgeMIScoreFilter extends ContinuousFilter<Edge> {

    public EdgeMIScoreFilter(NetworkView view, QueryFilters queryFilters) {
        super(view,
                Edge.class,
                "MI Score",
                "MI Score is a heuristic scoring system that weights the amount of evidence behind a pair if " +
                        "interacting molecules. Further information can be found in " +
                        "<a href=\"https://europepmc.org/articles/PMC4316181/\">Villaveces et al, Database 2015</a>",
                0,
                1,
                queryFilters != null ? queryFilters.getMinMIScore() : null,
                queryFilters != null ? queryFilters.getMaxMIScore() : null);
    }

    @Override
    public double getProperty(Edge element) {
        return element.miScore;
    }
}
