//package uk.ac.ebi.intact.app.internal.model.filters.node;
//
//import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
//import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
//import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
//import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;
//
//import static uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NodeFields.*;
//
//public class NodeOrthologGroup extends DiscreteFilter<Node> {
//    public NodeOrthologGroup(NetworkView view) {
//        super(view, Node.class, "Ortholog group database", "Database used for orthology grouping");
//    }
//
//    @Override
//    public String getPropertyValue(Node element) {
//        return ORTHOLOG_GROUP_ID.getValue(element.nodeRow).toString(); //todo: check to retrieve all the different dbs to add them here
//    }
//}
