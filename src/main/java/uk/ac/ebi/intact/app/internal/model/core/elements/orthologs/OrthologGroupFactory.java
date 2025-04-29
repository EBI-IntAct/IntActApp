package uk.ac.ebi.intact.app.internal.model.core.elements.orthologs;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class OrthologGroupFactory implements CyGroupFactory {

    @Override
    public OrthologGroup createGroup(CyNetwork network, boolean register) {
        return new OrthologGroup(network);
    }

    @Override
    public OrthologGroup createGroup(CyNetwork network, List<CyNode> nodes, List<CyEdge> edges, boolean register) {
        OrthologGroup group = new OrthologGroup(network);
        group.addNodes(nodes);
        group.addEdges(edges);
        //todo: add register
        return group;
    }

    @Override
    public OrthologGroup createGroup(CyNetwork network, CyNode node, List<CyNode> nodes, List<CyEdge> edges, boolean register) {
        node = new OrthologGroup(network);
        ((OrthologGroup) node).addNodes(nodes);
        ((OrthologGroup) node).addEdges(edges);
        //todo: add register
        return (OrthologGroup) node;
    }

    @Override
    public OrthologGroup createGroup(CyNetwork network, CyNode node, boolean register) {
        node = new OrthologGroup(network);
        //todo: add register
        return (OrthologGroup) node;
    }

    private List<List<CyNode>> groupNodes(List<CyNode> nodes) {
        List<List<CyNode>> nodesByOrthologs = new ArrayList<>();

        List<Node> intactNodes = convertCyNodeListToIntactNodeList(nodes);
        sortNodesByOrthologs(intactNodes);

        String currentOrthologGroupId = null;
        List<CyNode> currentOrthologGroup = new ArrayList<>();
        for (Node intactNode : intactNodes) {
            if (Objects.equals(currentOrthologGroupId, intactNode.ac)){
                currentOrthologGroup.add(intactNode.cyNode);
            } else {
                currentOrthologGroupId = intactNode.ac;
                nodesByOrthologs.add(currentOrthologGroup);
                currentOrthologGroup = new ArrayList<>();
            }
        }

        return nodesByOrthologs;
    }

    private void sortNodesByOrthologs(List<Node> intactNodes) {
        intactNodes.sort(Comparator.comparing(node -> node.ac));
    }

    private List<Node> convertCyNodeListToIntactNodeList(List<CyNode> nodes) {
        List<Node> intactNodes = new ArrayList<>();
        for (CyNode node : nodes) {
            intactNodes.add((Node) node);
        }
        return intactNodes;
    }
}
