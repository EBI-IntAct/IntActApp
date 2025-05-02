package uk.ac.ebi.intact.app.internal.model.core.network;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.*;
import org.cytoscape.model.events.*;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.NodeCouple;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.Style;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NetworkFields;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NodeFields;
import uk.ac.ebi.intact.app.internal.ui.components.legend.NodeColorLegendEditor;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.Position;

public class Network implements AddedEdgesListener, AboutToRemoveEdgesListener, RemovedEdgesListener {
    @JsonIgnore
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    public final Manager manager;
    CyNetwork cyNetwork;
    CyTable edgeTable;
    CyTable nodeTable;
    CyTable featuresTable;
    CyTable identifiersTable;

    private final Map<CyNode, Node> nodes = new HashMap<>();
    // Summary edges
    private final Map<NodeCouple, List<CyEdge>> coupleToSummarizedEdges = new HashMap<>();
    private final Map<NodeCouple, SummaryEdge> summaryEdges = new HashMap<>();
    private final Map<CyEdge, EvidenceEdge> evidenceEdges = new HashMap<>();

    private final Set<String> taxIds = new HashSet<>();
    private final Set<String> interactorTypes = new HashSet<>();
    private final Map<String, String> speciesNameToId = new HashMap<>();
    private final Map<String, String> speciesIdToName = new HashMap<>();

    CyGroupFactory groupFactory;
    CyGroupManager groupManager;

    Map<String, CyGroup> cyGroups = new HashMap<>();

    public Network(Manager manager) {
        this.manager = manager;
        groupFactory = manager.utils.getService(CyGroupFactory.class);
        groupManager = manager.utils.getService(CyGroupManager.class);
    }

    public CyNetwork getCyNetwork() {
        return cyNetwork;
    }

    public void setNetwork(CyNetwork cyNetwork) {
        this.cyNetwork = cyNetwork;

        cyNetwork.getNodeList().forEach(node -> nodes.put(node, new Node(this, node)));

        edgeTable = cyNetwork.getDefaultEdgeTable();
        nodeTable = cyNetwork.getDefaultNodeTable();

        TableUtil.NullAndNonNullEdges identifiedOrNotEdges = TableUtil.splitNullAndNonNullEdges(cyNetwork, EdgeFields.AC);

        for (CyEdge evidenceCyEdge : identifiedOrNotEdges.nonNullEdges) {
            evidenceEdges.put(evidenceCyEdge, (EvidenceEdge) Edge.createEdge(this, evidenceCyEdge));
        }

        NodeCouple.putEdgesToCouples(evidenceEdges.keySet(), coupleToSummarizedEdges);

        if (identifiedOrNotEdges.nullEdges.size() > 0) {
            for (CyEdge existingEdge : identifiedOrNotEdges.nullEdges) {
                NodeCouple existingCouple = new NodeCouple(existingEdge);
                summaryEdges.put(existingCouple, (SummaryEdge) Edge.createEdge(this, existingEdge));
            }
        }
        updateSummaryEdges(coupleToSummarizedEdges.keySet());


        completeMissingNodeColorsFromTables(true, null);
        manager.utils.registerAllServices(this, new Properties());
    }

    public Set<String> getInteractorTypes() {
        return interactorTypes;
    }

    public Set<String> getTaxIds() {
        return new HashSet<>(taxIds);
    }

    public String getSpeciesId(String speciesName) {
        return speciesNameToId.getOrDefault(speciesName, null);
    }

    public boolean speciesExist(String speciesName) {
        return speciesNameToId.containsKey(speciesName);
    }

    public Set<String> getNonDefinedTaxon() {
        Set<String> availableTaxIds = new HashSet<>(taxIds);
        availableTaxIds.removeAll(StyleMapper.speciesColors.keySet());
        availableTaxIds.removeAll(StyleMapper.speciesColors.keySet().stream()
                .filter(taxId -> StyleMapper.taxIdToChildrenTaxIds.containsKey(taxId))
                .flatMap(taxId -> StyleMapper.taxIdToChildrenTaxIds.get(taxId).stream())
                .collect(toSet()));
        Set<String> nonDefinedTaxon = new HashSet<>();
        Set<String> userDefinedTaxIds = NodeColorLegendEditor.getDefinedTaxIds();
        for (String availableTaxId : availableTaxIds) {
            if (!userDefinedTaxIds.contains(availableTaxId))
                nonDefinedTaxon.add(speciesIdToName.get(availableTaxId));
        }
        return nonDefinedTaxon;
    }


    public void hideExpandedEdgesOnViewCreation(CyNetworkView networkView) {
        HideTaskFactory hideTaskFactory = manager.utils.getService(HideTaskFactory.class);
        manager.utils.execute(hideTaskFactory.createTaskIterator(networkView, null, evidenceEdges.keySet()));
        manager.data.addNetworkView(networkView, false);
    }

    public void completeMissingNodeColorsFromTables(boolean async, Runnable callback) {
        Runnable kingdomUpdater = () -> {
            for (Node node : nodes.values()) {
                interactorTypes.add(node.type.value);
                taxIds.add(node.taxId);
                speciesNameToId.put(node.species, node.taxId);
                speciesIdToName.put(node.taxId, node.species);
            }

            Map<String, Paint> addedTaxIds = StyleMapper.harvestKingdomsOf(getTaxIds(), true);

            for (Style style : manager.style.getStyles().values()) {
                style.updateTaxIdToNodePaintMapping(addedTaxIds);
            }
            if (callback != null) callback.run();
        };
        if (async) executor.execute(kingdomUpdater);
        else kingdomUpdater.run();
    }

    public void completeMissingNodeColorsFromInteractors(Map<String, List<Interactor>> interactorsToResolve) {
        executor.execute(() -> {
            Set<String> interactorTaxIds = new HashSet<>();
            interactorsToResolve.values().stream().flatMap(List::stream).forEach(interactor -> interactorTaxIds.add(interactor.taxId));
            Map<String, Paint> addedTaxIds = StyleMapper.harvestKingdomsOf(interactorTaxIds, true);
            for (Style style : manager.style.getStyles().values()) {
                style.updateTaxIdToNodePaintMapping(addedTaxIds);
            }
        });
    }

    private List<SummaryEdge> updateSummaryEdges(Collection<NodeCouple> couplesToUpdate) {
        CyEventHelper eventHelper = manager.utils.getService(CyEventHelper.class);
        CyTable table = cyNetwork.getDefaultEdgeTable();
        eventHelper.silenceEventSource(table);
        List<SummaryEdge> newSummaryEdges = new ArrayList<>();

        for (NodeCouple couple : couplesToUpdate) {
            CyEdge summaryCyEdge;
            List<CyEdge> summarizedEdges = coupleToSummarizedEdges.get(couple);
            if (!summarizedEdges.isEmpty()) {
                if (!summaryEdges.containsKey(couple)) {
                    summaryCyEdge = cyNetwork.addEdge(couple.node1, couple.node2, false);
                    CyRow summaryRow = table.getRow(summaryCyEdge.getSUID());

                    Map<Position, Set<String>> features = addSummarizedEdges(couple, summaryRow, summarizedEdges);

                    EdgeFields.FEATURES.SOURCE.setValue(summaryRow, new ArrayList<>(features.get(Position.SOURCE)));
                    EdgeFields.FEATURES.TARGET.setValue(summaryRow, new ArrayList<>(features.get(Position.TARGET)));
                    EdgeFields.NAME.setValue(summaryRow, TableUtil.getName(cyNetwork, couple.node1) + " (interact with) " + TableUtil.getName(cyNetwork, couple.node2));

                    CyRow firstEdgeRow = cyNetwork.getRow(summarizedEdges.get(0));
                    EdgeFields.MI_SCORE.setValue(summaryRow, EdgeFields.MI_SCORE.getValue(firstEdgeRow));
                    EdgeFields.IS_SUMMARY.setValue(summaryRow, true);
                    EdgeFields.SUMMARY_NB_EDGES.setValue(summaryRow, summarizedEdges.size());

                    SummaryEdge summaryEdge = (SummaryEdge) Edge.createEdge(this, summaryCyEdge);
                    summaryEdges.put(couple, summaryEdge);
                    newSummaryEdges.add(summaryEdge);
                } else {
                    SummaryEdge summaryEdge = summaryEdges.get(couple);
                    addSummarizedEdges(couple, summaryEdge.edgeRow, summarizedEdges);
                    summaryEdge.updateSummary();
                }
            } else {
                summaryCyEdge = summaryEdges.get(couple).cyEdge;
                summaryEdges.remove(couple);
                cyNetwork.removeEdges(Collections.singleton(summaryCyEdge));
            }
        }
        eventHelper.unsilenceEventSource(table);
        return newSummaryEdges;
    }

    private Map<Position, Set<String>> addSummarizedEdges(NodeCouple couple, CyRow summaryRow, List<CyEdge> summarizedEdges) {
        Set<String> sourceFeatures = new HashSet<>();
        Set<String> targetFeatures = new HashSet<>();

        List<Long> summarizedEdgesSUID = EdgeFields.SUMMARIZED_EDGES_SUID.getValue(summaryRow);
        Set<Long> summarizedEdgesSUIDSet = new HashSet<>(summarizedEdgesSUID);
        for (CyEdge edge : summarizedEdges) {
            CyRow edgeRow = edgeTable.getRow(edge.getSUID());
            if (!summarizedEdgesSUIDSet.contains(edge.getSUID())) summarizedEdgesSUID.add(edge.getSUID());
            List<String> edgeSourceFeatures = EdgeFields.FEATURES.SOURCE.getValue(edgeRow);
            List<String> edgeTargetFeatures = EdgeFields.FEATURES.TARGET.getValue(edgeRow);
            if (edge.getSource().equals(couple.node1)) {
                if (edgeSourceFeatures != null) sourceFeatures.addAll(edgeSourceFeatures);
                if (edgeTargetFeatures != null) targetFeatures.addAll(edgeTargetFeatures);
            } else {
                if (edgeTargetFeatures != null) sourceFeatures.addAll(edgeTargetFeatures);
                if (edgeSourceFeatures != null) targetFeatures.addAll(edgeSourceFeatures);
            }
        }

        Map<Position, Set<String>> features = new HashMap<>();
        features.put(Position.SOURCE, sourceFeatures);
        features.put(Position.TARGET, targetFeatures);
        return features;
    }


    @Override
    public void handleEvent(AddedEdgesEvent e) {
        if (e.getSource() == cyNetwork) {
            Collection<CyEdge> addedEdges = e.getPayloadCollection();
            List<CyEdge> addedEvidenceEdges = new ArrayList<>();
            List<CyEdge> edgesToHide = new ArrayList<>();

            for (CyEdge addedEdge : addedEdges) {
                NodeCouple couple = new NodeCouple(addedEdge);
                Edge edge = Edge.createEdge(this, addedEdge);
                if (edge == null) continue;
                if (edge instanceof EvidenceEdge) {

                    evidenceEdges.put(addedEdge, (EvidenceEdge) edge);
                    addedEvidenceEdges.add(addedEdge);
                    if (summaryEdges.containsKey(couple)) summaryEdges.get(couple).updateSummary();

                } else if (edge instanceof SummaryEdge) {

                    SummaryEdge summaryEdge = (SummaryEdge) edge;
                    summaryEdges.put(couple, summaryEdge);
                    if (deletionChain.containsKey(addedEdge)) {
                        Set<EvidenceEdge> evidenceEdgesToAdd = deletionChain.remove(addedEdge);
                        List<CyEdge> summarizedCyEdges = new ArrayList<>();
                        List<Long> summarizedEdges = EdgeFields.SUMMARIZED_EDGES_SUID.getValue(cyNetwork.getRow(addedEdge));

                        for (EvidenceEdge edgeToAdd : evidenceEdgesToAdd) {
                            if (edgeToAdd == null) continue;

                            EvidenceEdge evidenceEdge = edgeToAdd.cloneInto(this);
                            evidenceEdges.put(evidenceEdge.cyEdge, evidenceEdge);
                            summarizedCyEdges.add(evidenceEdge.cyEdge);
                            summarizedEdges.add(evidenceEdge.cyEdge.getSUID());

                        }
                        coupleToSummarizedEdges.put(couple, summarizedCyEdges);
                        edgesToHide.addAll(summarizedCyEdges);
                        summaryEdge.updateSummary();
                    }
                }
            }
            Set<NodeCouple> updatedCouples = NodeCouple.putEdgesToCouples(addedEvidenceEdges, coupleToSummarizedEdges);
            updateSummaryEdges(updatedCouples).forEach(summaryEdge -> edgesToHide.add(summaryEdge.cyEdge));

            if (!edgesToHide.isEmpty()) {
                HideTaskFactory hideTaskFactory = manager.utils.getService(HideTaskFactory.class);
                manager.data.getNetworkViews(this).forEach(view -> {
                    view.cyView.updateView();
                    manager.utils.execute(hideTaskFactory.createTaskIterator(view.cyView, new ArrayList<>(), edgesToHide));
                });
            }
        }
    }

    private boolean removing = false;
    private final List<Node> nodesToUpdate = new ArrayList<>();
    private final Map<CyEdge, Set<EvidenceEdge>> deletionChain = new HashMap<>();

    @Override
    public void handleEvent(AboutToRemoveEdgesEvent e) {
        if (removing) return;
        if (e.getSource() == cyNetwork) {
            Collection<CyEdge> removedEdges = e.getEdges();
            for (CyEdge removedEdge : removedEdges) {
                NodeCouple nodeCouple = new NodeCouple(removedEdge);
                if (evidenceEdges.containsKey(removedEdge)) {
                    evidenceEdges.remove(removedEdge);
                    List<CyEdge> summarizedEdges = coupleToSummarizedEdges.get(nodeCouple);
                    summarizedEdges.remove(removedEdge);
                    SummaryEdge summaryEdge = summaryEdges.get(nodeCouple);
                    if (summarizedEdges.isEmpty()) {
                        if (summaryEdge != null) cyNetwork.removeEdges(Collections.singleton(summaryEdge.cyEdge));
                    } else summaryEdge.updateSummary();
                } else if (summaryEdges.containsKey(nodeCouple)) {
                    List<CyEdge> summarizedEdges = coupleToSummarizedEdges.get(nodeCouple);
                    Set<EvidenceEdge> removedSummarizedEdges = summarizedEdges.stream().map(evidenceEdges::remove).filter(Objects::nonNull).collect(toSet());
                    removing = true;
                    deletionChain.put(removedEdge, removedSummarizedEdges);
                    cyNetwork.removeEdges(summarizedEdges);
                    removing = false;
                    coupleToSummarizedEdges.remove(nodeCouple);
                    summaryEdges.remove(nodeCouple);
                }
                nodesToUpdate.add(getNode(removedEdge.getSource()));
                nodesToUpdate.add(getNode(removedEdge.getTarget()));
            }
        }
    }

    @Override
    public void handleEvent(RemovedEdgesEvent e) {
        while (!nodesToUpdate.isEmpty()) {
            Node node = nodesToUpdate.remove(nodesToUpdate.size() - 1);
            if (node != null) node.updateMutationStatus();
        }
    }

    public CyTable getFeaturesTable() {
        if (featuresTable != null) return featuresTable;
        if (cyNetwork == null) return null;
        Long tableSUID = NetworkFields.FEATURES_TABLE_REF.getValue(cyNetwork.getRow(cyNetwork));
        if (tableSUID == null) return null;
        return manager.utils.getService(CyTableManager.class).getTable(tableSUID);
    }

    public void setFeaturesTable(CyTable featuresTable) {
        this.featuresTable = featuresTable;
        if (cyNetwork != null) {
            NetworkFields.FEATURES_TABLE_REF.setValue(cyNetwork.getRow(cyNetwork), featuresTable.getSUID());
        }
    }

    public CyTable getIdentifiersTable() {
        if (identifiersTable != null) return identifiersTable;
        if (cyNetwork == null) return null;
        Long tableSUID = NetworkFields.IDENTIFIERS_TABLE_REF.getValue(cyNetwork.getRow(cyNetwork));
        if (tableSUID == null) return null;
        return manager.utils.getService(CyTableManager.class).getTable(tableSUID);
    }

    public void setIdentifiersTable(CyTable identifiersTable) {
        this.identifiersTable = identifiersTable;
        if (cyNetwork != null) {
            NetworkFields.IDENTIFIERS_TABLE_REF.setValue(cyNetwork.getRow(cyNetwork), identifiersTable.getSUID());
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Network that = (Network) o;

        return cyNetwork.equals(that.cyNetwork);
    }

    @Override
    public int hashCode() {
        return cyNetwork.hashCode();
    }


    @Override
    public String toString() {
        return cyNetwork.getRow(cyNetwork).get(CyNetwork.NAME, String.class);
    }

    public List<CyNode> getSelectedCyNodes() {
        List<CyNode> selectedNodes = new ArrayList<>();
        for (CyNode node : cyNetwork.getNodeList()) {
            if (cyNetwork.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
                selectedNodes.add(node);
            }
        }
        return selectedNodes;
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public Node getNode(CyNode cyNode) {
        return nodes.get(cyNode);
    }

    public List<CyEdge> getSelectedCyEdges() {
        List<CyEdge> selectedEdges = new ArrayList<>();
        for (CyEdge edge : cyNetwork.getEdgeList()) {
            if (cyNetwork.getRow(edge).get(CyNetwork.SELECTED, Boolean.class)) {
                selectedEdges.add(edge);
            }
        }
        return selectedEdges;
    }

    public Edge getEdge(CyEdge cyEdge) {
        if (evidenceEdges.containsKey(cyEdge)) return evidenceEdges.get(cyEdge);
        else {
            CyRow row = cyNetwork.getRow(cyEdge);
            if (row != null && EdgeFields.IS_SUMMARY.getValue(row)) return summaryEdges.get(new NodeCouple(cyEdge));
        }
        return null;
    }


    public List<SummaryEdge> getSummaryEdges() {
        return new ArrayList<>(summaryEdges.values());
    }

    public List<CyEdge> getSummaryCyEdges() {
        return summaryEdges.values().stream().map(summaryEdge -> summaryEdge.cyEdge).collect(toList());
    }

    public SummaryEdge getSummaryEdge(CyEdge edge) {
        return summaryEdges.get(new NodeCouple(edge));
    }


    public List<EvidenceEdge> getEvidenceEdges() {
        return new ArrayList<>(evidenceEdges.values());
    }

    public List<CyEdge> getEvidenceCyEdges() {
        return new ArrayList<>(evidenceEdges.keySet());
    }

    public List<CyEdge> getSimilarEvidenceCyEdges(CyEdge cyEdge) {
        return coupleToSummarizedEdges.get(new NodeCouple(cyEdge));
    }

    public EvidenceEdge getEvidenceEdge(CyEdge edge) {
        return evidenceEdges.get(edge);
    }


    public CyEdge getCyEdge(Long suid) {
        return cyNetwork.getEdge(suid);
    }

    public Long getSUID(CyRow row) {
        if (row == null) return null;
        return row.get(CyNetwork.SUID, Long.class);
    }

    public CyRow getCyRow(CyIdentifiable element) {
        return cyNetwork.getRow(element);
    }

    public CyRow getCyRow() {
        return cyNetwork.getRow(cyNetwork);
    }

    public Map<String, List<CyNode>> groupNodesByProperty(String columnName) {
        Map<String, List<CyNode>> groups = new HashMap<>();

        for (CyNode cyNode : cyNetwork.getNodeList()) {
            CyRow row = cyNetwork.getRow(cyNode);
            String value = row.get(columnName, String.class);
            if (value == null) value = "Undefined";

            groups.computeIfAbsent(value, k -> new ArrayList<>()).add(cyNode);
        }

        return groups;
    }

    public void createGroupsByProperty(String columnName) {
        Map<String, List<CyNode>> groups = groupNodesByProperty(columnName);

        groups.forEach((key, cyNodes) -> {
            if (cyNodes.size() > 1) {
                CyGroup group = groupFactory.createGroup(cyNetwork, cyNodes, null, true);
                groupManager.addGroup(group);
                cyGroups.put(key, group);
            }
        });
    }

    public void collapseGroups(String columnName) {
        //todo: check to use the columnName to collapse by property
        cyGroups.forEach((key, group) -> {
            group.collapse(cyNetwork);
            CyRow row = getCyRow(group.getGroupNode());
            if (row != null) {
                row.set(NodeFields.NAME.name, key);
                row.set(columnName, key);
//                row.set(NodeFields.TYPE, new CVField()) todo: check what we want for type (https://www.ebi.ac.uk/ols4/ontologies/mi/classes/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FMI_2426)
            }
        });
    }


    public void expandGroups(String columnName) {
        //todo: check to use the columnName to expand by property
        cyGroups.forEach((key, group) -> group.expand(cyNetwork));
    }

}
