package uk.ac.ebi.intact.app.internal.model.core.elements.orthologs;

import lombok.Getter;
import lombok.Setter;
import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class OrthologGroup implements CyGroup, CyNode {
    CyNetwork parentNetwork;
    List<CyNode> nodeList;

    //todo: for the edges: check if it works correctly for summarised edges
    List<CyEdge> internalEdgeList; // edges of nodes within the group
    List<CyEdge> externalEdgeList; // edges from node in the group to node outside the group

    boolean isCollapsed = false;
    CyNode groupNode;

    public OrthologGroup(CyNetwork parentNetwork) {
        this.parentNetwork = parentNetwork;
        groupNode = parentNetwork.addNode();
    }

    @Override
    public CyNetwork getGroupNetwork() {
        //todo: check if we need to create a sub-network
        return parentNetwork;
    }

    @Override
    public void addNodes(List<CyNode> nodes) {
        //todo: check if it adds the edges automatically or not
        nodeList.addAll(nodes);
    }

    @Override
    public void addEdges(List<CyEdge> edges) {
        for (CyEdge edge : edges) {
            if (nodeList.contains(edge.getSource()) && nodeList.contains(edge.getTarget())) {
                internalEdgeList.add(edge);
            } else {
                externalEdgeList.add(edge);
            }
        }
    }

    @Override
    public void removeNodes(List<CyNode> nodes) {
        nodeList.removeAll(nodes);
    }

    @Override
    public void removeEdges(List<CyEdge> edges) {
        internalEdgeList.removeAll(edges);
    }

    @Override
    public CyRootNetwork getRootNetwork() {
        return null;
    }

    @Override
    public void addGroupToNetwork(CyNetwork network) {
        network.getNodeList().add(groupNode);
    }

    @Override
    public void removeGroupFromNetwork(CyNetwork network) {
        network.getNodeList().remove(groupNode);
    }

    @Override
    public Set<CyNetwork> getNetworkSet() {
        //return all the networks the node is contained in
        return Set.of();
    }

    @Override
    public boolean isInNetwork(CyNetwork network) {
        return network.getNodeList().contains(groupNode);
    }

    @Override
    public void collapse(CyNetwork network) {
        isCollapsed = true;
        network.getNodeList().removeIf(node -> nodeList.contains(node));
        network.getEdgeList().removeIf(edge -> internalEdgeList.contains(edge));
        addGroupToNetwork(network);
        //todo: check for the externalEdgeList
    }

    @Override
    public void expand(CyNetwork network) {
        isCollapsed = false;
        network.getNodeList().removeIf(node -> node.equals(groupNode));
        network.getNodeList().addAll(nodeList);
        network.getEdgeList().addAll(internalEdgeList);
        network.getEdgeList().addAll(externalEdgeList);
    }

    @Override
    public boolean isCollapsed(CyNetwork network) {
        return isCollapsed;
    }

    @Override
    public CyNetwork getNetworkPointer() {
        return null;
    }

    @Override
    public void setNetworkPointer(CyNetwork network) {

    }

    @Override
    public Long getSUID() {
        return 0L;
    }
}
