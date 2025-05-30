package uk.ac.ebi.intact.app.internal.model.filters.node;

import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.RadioButtonFilter;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NodeFields;

public class NodeOrthologGroup extends RadioButtonFilter<Node> {

    public NodeOrthologGroup(NetworkView view) {
        super(view,
                "Ortholog Group database",
                "Select which database to use for orthology grouping",
                Node.class,
                NodeFields.ORTHOLOG_GROUP_ID.name,
                "panther"
        );
    }
}
