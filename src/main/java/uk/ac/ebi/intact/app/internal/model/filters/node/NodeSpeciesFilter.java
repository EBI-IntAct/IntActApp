package uk.ac.ebi.intact.app.internal.model.filters.node;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryFilters;

public class NodeSpeciesFilter extends DiscreteFilter<Node> {

    public NodeSpeciesFilter(NetworkView view, QueryFilters queryFilters) {
        super(view,
                Node.class,
                "Species",
                "Organism of origin of the selected molecule",
                queryFilters != null ? queryFilters.getInteractorSpeciesFilter() : null);
    }

    @Override
    public String getPropertyValue(Node element) {
        return element.species;
    }
}
