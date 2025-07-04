package uk.ac.ebi.intact.app.internal.model.filters.node;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryFilters;

import java.util.Map;

import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.mergeOrganismLabelAndTaxIdAsId;
import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.mergeOrganismLabelAndTaxIdAsLabel;

public class NodeSpeciesFilter extends DiscreteFilter<Node> {

    public NodeSpeciesFilter(NetworkView view, QueryFilters queryFilters) {
        super(view,
                Node.class,
                "Species",
                "Organism of origin of the selected molecule",
                queryFilters != null ? queryFilters.getInteractorSpeciesFilter() : null);
    }

    @Override
    public Map<String, String> getPropertyValues(Node element) {
        if (element.taxId != null && element.species != null) {
            return Map.of(
                    mergeOrganismLabelAndTaxIdAsId(element.species, element.taxId),
                    mergeOrganismLabelAndTaxIdAsLabel(element.species, element.taxId));
        } else {
            return Map.of();
        }
    }
}
