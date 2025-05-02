package uk.ac.ebi.intact.app.internal.model.filters.node;

import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NodeFields;

public class OrthologGroupFilter extends BooleanFilter<Node> {
    Network network;

    public OrthologGroupFilter(NetworkView networkView) {
        super(networkView,
                Node.class,
                "Group by orthologs",
                "",
                "");
        this.network = networkView.getNetwork();
        network.createGroupsByProperty(NodeFields.ORTHOLOG_GROUP_ID.name);
    }

    @Override
    public boolean isToHide(Node element) {
        return false;
    }

    @Override
    public void setStatus(boolean status) {
        super.setStatus(status);
        fireFilterUpdated();
        if (status) {
            network.collapseGroups(NodeFields.ORTHOLOG_GROUP_ID.name);
        }
        else {
            network.expandGroups(NodeFields.ORTHOLOG_GROUP_ID.name);
        }
    }

}
