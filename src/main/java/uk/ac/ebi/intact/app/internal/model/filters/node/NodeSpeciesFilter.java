package uk.ac.ebi.intact.app.internal.model.filters.node;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class NodeSpeciesFilter extends DiscreteFilter<Node> {

    public NodeSpeciesFilter(NetworkView view) {
        super(view,
                Node.class,
                "Species",
                "Organism of origin of the selected molecule",
                view.getNetwork().getQueryParams() != null ? view.getNetwork().getQueryParams().getInteractorSpeciesFilter() : null);
    }

    @Override
    public String getPropertyValue(Node element) {
        return element.species;
    }
}
