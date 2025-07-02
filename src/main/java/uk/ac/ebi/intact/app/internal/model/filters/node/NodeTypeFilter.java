package uk.ac.ebi.intact.app.internal.model.filters.node;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryFilters;

import java.util.Map;

public class NodeTypeFilter extends DiscreteFilter<Node> {
    public NodeTypeFilter(NetworkView view, QueryFilters queryFilters) {
        super(view,
                Node.class,
                "Type",
                "Interactor molecule type (protein, nucleic acid, small molecule, complex...) involved in the interaction",
                queryFilters != null ? queryFilters.getInteractorTypesFilter() : null);
    }

    @Override
    public Map<String, String> getPropertyValues(Node node) {
        if (node.type != null && node.type.id != null && node.type.id.id != null && node.typeName != null && node.typeName != null) {
            return Map.of(node.type.id.id, node.typeName);
        } else {
            return Map.of();
        }
    }
}
