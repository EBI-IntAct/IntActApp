package uk.ac.ebi.intact.app.internal.model.core.network;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.*;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.NodeCouple;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.Style;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.NetworkFields;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.NodeFields;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Network implements AddedEdgesListener, AboutToRemoveEdgesListener {
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    final Manager manager;
    CyNetwork cyNetwork;
    Map<String, List<String>> termToAcs;
    Map<String, List<Interactor>> interactorsToResolve;
    Map<String, Integer> totalInteractors = new HashMap<>();
    CyTable edgeTable;
    CyTable nodeTable;
    CyTable featuresTable;
    CyTable identifiersTable;

    private final List<Node> iNodes = new ArrayList<>();
    // Summary edges
    private final Map<NodeCouple, List<CyEdge>> coupleToEdges = new HashMap<>();
    private final Map<NodeCouple, CyEdge> summaryCyEdges = new HashMap<>();
    private final List<SummaryEdge> summaryEdges = new ArrayList<>();
    private final List<CyEdge> evidenceCyEdges = new ArrayList<>();
    private final List<EvidenceEdge> evidenceEdges = new ArrayList<>();

    private final Set<Long> taxIds = new HashSet<>();
    private final Set<String> interactorTypes = new HashSet<>();
    private final Map<String, Long> speciesNameToId = new HashMap<>();
    private final Map<Long, String> speciesIdToName = new HashMap<>();


    public Network(Manager manager) {
        this.manager = manager;
        termToAcs = null;
        interactorsToResolve = null;
    }

    public Manager getManager() {
        return manager;
    }

    public CyNetwork getCyNetwork() {
        return cyNetwork;
    }

    public void setNetwork(CyNetwork cyNetwork) {
        this.cyNetwork = cyNetwork;

        edgeTable = cyNetwork.getDefaultEdgeTable();
        nodeTable = cyNetwork.getDefaultNodeTable();

        TableUtil.NullAndNonNullEdges identifiedOrNotEdges = TableUtil.splitNullAndNonNullEdges(cyNetwork, EdgeFields.AC);

        evidenceCyEdges.addAll(identifiedOrNotEdges.nonNullEdges);

        NodeCouple.putEdgesToCouples(evidenceCyEdges, coupleToEdges);

        if (identifiedOrNotEdges.nullEdges.size() > 0) {
            for (CyEdge existingEdge : identifiedOrNotEdges.nullEdges) {
                NodeCouple existingCouple = new NodeCouple(existingEdge);
                summaryCyEdges.put(existingCouple, existingEdge);
            }
        } else {
            updateSummaryEdges(coupleToEdges.keySet());
        }

        cyNetwork.getNodeList().forEach(node -> iNodes.add(new Node(this, node)));
        summaryCyEdges.values().forEach(edge -> summaryEdges.add((SummaryEdge) Edge.createIntactEdge(this, edge)));
        evidenceCyEdges.forEach(edge -> evidenceEdges.add((EvidenceEdge) Edge.createIntactEdge(this, edge)));

        completeMissingNodeColorsFromTables();
    }

    public Set<String> getInteractorTypes() {
        return interactorTypes;
    }

    public Set<Long> getTaxIds() {
        return new HashSet<>(taxIds);
    }

    public Long getSpeciesId(String speciesName) {
        return speciesNameToId.getOrDefault(speciesName, null);
    }

    public Set<String> getNonDefinedTaxon() {
        Set<Long> availableTaxIds = new HashSet<>(taxIds);
        availableTaxIds.removeAll(StyleMapper.taxIdToPaint.keySet());
        Set<String> nonDefinedTaxon = new HashSet<>();
        for (Long availableTaxId : availableTaxIds) {
            nonDefinedTaxon.add(speciesIdToName.get(availableTaxId));
        }
        return nonDefinedTaxon;
    }


    public Map<String, List<Interactor>> resolveTerms(final String terms, boolean exactQuery) {
        Map<Object, Object> resolverData = new HashMap<>();
        resolverData.put("query", terms.replaceAll("\n", ",")); //TODO Remove replacement when backend handle \n
        resolverData.put("fuzzySearch", !exactQuery);
        resolverData.put("pageSize", manager.option.MAX_INTERACTOR_PER_TERM.getValue());
        interactorsToResolve = Interactor.getInteractorsToResolve(HttpUtils.postJSON(Manager.INTACT_INTERACTOR_WS + "list/resolve", resolverData, manager), totalInteractors);
        completeMissingNodeColorsFromInteractors();
        return interactorsToResolve;
    }

    public Map<String, List<Interactor>> completeMissingInteractors(List<String> termsToComplete, boolean exactQuery) {
        HashMap<String, List<Interactor>> newInteractors = new HashMap<>();
        completeMissingInteractors(termsToComplete, exactQuery, newInteractors, 1);
        return newInteractors;
    }

    private void completeMissingInteractors(List<String> termsToComplete, boolean exactQuery, Map<String, List<Interactor>> newInteractors, int page) {
        Map<Object, Object> resolverData = new HashMap<>();
        resolverData.put("query", String.join("\n", termsToComplete));
        resolverData.put("fuzzySearch", !exactQuery);
        resolverData.put("pageSize", manager.option.MAX_INTERACTOR_PER_TERM.getValue());
        resolverData.put("page", page);
        Map<String, List<Interactor>> additionalInteractors = Interactor.getInteractorsToResolve(HttpUtils.postJSON(Manager.INTACT_INTERACTOR_WS + "list/resolve", resolverData, manager), totalInteractors);
        termsToComplete.removeIf(term -> additionalInteractors.get(term).isEmpty());
        additionalInteractors.forEach((term, interactors) -> {
            CollectionUtils.addAllToGroups(newInteractors, interactors, interactor -> term);
            CollectionUtils.addAllToGroups(interactorsToResolve, interactors, interactor -> term);
        });
        if (!termsToComplete.isEmpty())
            completeMissingInteractors(termsToComplete, exactQuery, newInteractors, page + 1);

    }


    public Map<String, List<Interactor>> getInteractorsToResolve() {
        return interactorsToResolve;
    }

    public Map<String, Integer> getTotalInteractors() {
        return totalInteractors;
    }

    public boolean hasNoAmbiguity() {
        if (termToAcs == null) termToAcs = new HashMap<>();
        boolean noAmbiguity = true;
        for (String key : interactorsToResolve.keySet()) {
            List<Interactor> interactors = interactorsToResolve.get(key);
            if (interactors.size() > 1) {
                noAmbiguity = false;
                break;
            } else if (interactors.size() == 1) {
                List<String> ids = new ArrayList<>();
                ids.add(interactors.get(0).ac);
                termToAcs.put(key, ids);
            } else {
                termToAcs.put(key, new ArrayList<>());
            }
        }

        return noAmbiguity;
    }

    public List<String> combineAcs(Map<String, String> acToTerm) {
        List<String> acs = new ArrayList<>();
        for (String term : termToAcs.keySet()) {
            for (String ac : termToAcs.get(term)) {
                acs.add(ac);
                acToTerm.put(ac, term);
            }
        }
        return acs;
    }

    public void hideExpandedEdgesOnViewCreation(CyNetworkView networkView) {
        HideTaskFactory hideTaskFactory = manager.utils.getService(HideTaskFactory.class);
        manager.utils.execute(hideTaskFactory.createTaskIterator(networkView, null, evidenceCyEdges));
        manager.data.addNetworkView(networkView, false);
    }

    public void completeMissingNodeColorsFromTables() {
        executor.execute(() -> {
            for (CyRow row : nodeTable.getAllRows()) {
                interactorTypes.add(NodeFields.TYPE.getValue(row));
                Long taxId = NodeFields.TAX_ID.getValue(row);
                taxIds.add(taxId);
                String specieName = NodeFields.SPECIES.getValue(row);
                speciesNameToId.put(specieName, taxId);
                speciesIdToName.put(taxId, specieName);
            }

            Map<Long, Paint> addedTaxIds = StyleMapper.completeTaxIdColorsFromUnknownTaxIds(getTaxIds());

            for (Style style : manager.style.getIntactStyles().values()) {
                style.updateTaxIdToNodePaintMapping(addedTaxIds);
            }
        });
    }

    public void completeMissingNodeColorsFromInteractors() {
        executor.execute(() -> {
            interactorsToResolve.values().stream().flatMap(List::stream).forEach(interactor -> {
                interactorTypes.add(interactor.type);
                Long taxId = interactor.taxId;
                taxIds.add(taxId);
                String specieName = interactor.species;
                speciesNameToId.put(specieName, taxId);
                speciesIdToName.put(taxId, specieName);
            });

            Map<Long, Paint> addedTaxIds = StyleMapper.completeTaxIdColorsFromUnknownTaxIds(getTaxIds());

            for (Style style : manager.style.getIntactStyles().values()) {
                style.updateTaxIdToNodePaintMapping(addedTaxIds);
            }
        });
    }

    private void updateSummaryEdges(Collection<NodeCouple> couplesToUpdate) {
        CyEventHelper eventHelper = manager.utils.getService(CyEventHelper.class);
        CyTable table = cyNetwork.getDefaultEdgeTable();
        eventHelper.silenceEventSource(table);
        for (NodeCouple couple : couplesToUpdate) {
            CyEdge summaryEdge;
            List<CyEdge> similarEdges = coupleToEdges.get(couple);
            if (!similarEdges.isEmpty()) {
                if (!summaryCyEdges.containsKey(couple)) {
                    summaryEdge = cyNetwork.addEdge(couple.node1, couple.node2, false);
                    summaryCyEdges.put(couple, summaryEdge);
                } else {
                    summaryEdge = summaryCyEdges.get(couple);
                }
                CyRow summaryEdgeRow = table.getRow(summaryEdge.getSUID());


                Set<String> sourceFeatures = new HashSet<>();
                Set<String> targetFeatures = new HashSet<>();
                for (CyEdge edge : similarEdges) {
                    CyRow edgeRow = edgeTable.getRow(edge.getSUID());

                    List<String> edgeSourceFeatures = EdgeFields.SOURCE_FEATURES.getValue(edgeRow);
                    List<String> edgeTargetFeatures = EdgeFields.TARGET_FEATURES.getValue(edgeRow);
                    if (edge.getSource().equals(couple.node1)) {
                        if (edgeSourceFeatures != null) sourceFeatures.addAll(edgeSourceFeatures);
                        if (edgeTargetFeatures != null) targetFeatures.addAll(edgeTargetFeatures);
                    } else {
                        if (edgeTargetFeatures != null) sourceFeatures.addAll(edgeTargetFeatures);
                        if (edgeSourceFeatures != null) targetFeatures.addAll(edgeSourceFeatures);
                    }
                }
                EdgeFields.SOURCE_FEATURES.setValue(summaryEdgeRow, new ArrayList<>(sourceFeatures));
                EdgeFields.TARGET_FEATURES.setValue(summaryEdgeRow, new ArrayList<>(targetFeatures));
                EdgeFields.NAME.setValue(summaryEdgeRow, TableUtil.getName(cyNetwork, couple.node1) + " (interact with) " + TableUtil.getName(cyNetwork, couple.node2));

                CyRow firstEdgeRow = cyNetwork.getRow(similarEdges.get(0));
                EdgeFields.MI_SCORE.setValue(summaryEdgeRow, EdgeFields.MI_SCORE.getValue(firstEdgeRow));
                EdgeFields.IS_SUMMARY.setValue(summaryEdgeRow, true);
                EdgeFields.SUMMARY_NB_EDGES.setValue(summaryEdgeRow, similarEdges.size());
                EdgeFields.SUMMARY_EDGES_ID.setValue(summaryEdgeRow, TableUtil.getFieldValuesOfEdges(edgeTable, EdgeFields.ID, similarEdges, null));
                EdgeFields.SUMMARY_EDGES_SUID.setValue(summaryEdgeRow, TableUtil.getFieldValuesOfEdges(edgeTable, EdgeFields.SUID, similarEdges, null));

            } else {
                summaryEdge = summaryCyEdges.get(couple);
                cyNetwork.removeEdges(Collections.singleton(summaryEdge));
            }
        }
        eventHelper.unsilenceEventSource(table);
    }

    public List<SummaryEdge> getSummaryEdges() {
        return new ArrayList<>(summaryEdges);
    }

    public List<EvidenceEdge> getEvidenceEdges() {
        return new ArrayList<>(evidenceEdges);
    }

    public List<CyEdge> getSummaryCyEdges() {
        return new ArrayList<>(summaryCyEdges.values());
    }

    public List<CyEdge> getEvidenceCyEdges() {
        return new ArrayList<>(evidenceCyEdges);
    }

    public List<CyEdge> getSimilarEvidenceEdges(CyEdge edge) {
        return coupleToEdges.get(new NodeCouple(edge));
    }

    public CyEdge getSummaryEdge(CyEdge edge) {
        return summaryCyEdges.get(new NodeCouple(edge));
    }


    @Override
    public void handleEvent(AddedEdgesEvent e) {
        if (e.getSource() == cyNetwork) {
            Collection<CyEdge> addedEdges = e.getPayloadCollection();
            evidenceCyEdges.addAll(addedEdges);
//            expandedIEdges.addAll(addedEdges.stream().map(edge -> (IntactEvidenceEdge) IntactEdge.createIntactEdge(this, edge)).filter(Objects::nonNull).collect(Collectors.toList()));
            Set<NodeCouple> updatedCouples = NodeCouple.putEdgesToCouples(addedEdges, coupleToEdges);
            updateSummaryEdges(updatedCouples);
        }
    }


    @Override
    public void handleEvent(AboutToRemoveEdgesEvent e) {
        if (e.getSource() == cyNetwork) {
            Collection<CyEdge> removedEdges = e.getEdges();
            evidenceCyEdges.removeAll(removedEdges);
            Map<NodeCouple, List<CyEdge>> couplesToRemove = new HashMap<>();
            NodeCouple.putEdgesToCouples(removedEdges, couplesToRemove);
            couplesToRemove.forEach((couple, cyEdges) -> coupleToEdges.get(couple).removeAll(cyEdges));
            updateSummaryEdges(couplesToRemove.keySet());
        }
    }


    public CyTable getFeaturesTable() {
        return featuresTable;
    }

    public void setFeaturesTable(CyTable featuresTable) {
        this.featuresTable = featuresTable;
        if (cyNetwork != null) {
            NetworkFields.FEATURES_TABLE_REF.setValue(cyNetwork.getRow(cyNetwork), featuresTable.getSUID());
        }
    }

    public CyTable getIdentifiersTable() {
        return identifiersTable;
    }

    public void setIdentifiersTable(CyTable identifiersTable) {
        this.identifiersTable = identifiersTable;
        if (cyNetwork != null) {
            NetworkFields.IDENTIFIERS_TABLE_REF.setValue(cyNetwork.getRow(cyNetwork), identifiersTable.getSUID());
        }
    }

    public List<CyEdge> getSelectedEdges() {
        List<CyEdge> selectedEdges = new ArrayList<>();
        for (CyEdge edge : cyNetwork.getEdgeList()) {
            if (cyNetwork.getRow(edge).get(CyNetwork.SELECTED, Boolean.class)) {
                selectedEdges.add(edge);
            }
        }
        return selectedEdges;
    }

    public List<CyNode> getSelectedNodes() {
        List<CyNode> selectedNodes = new ArrayList<>();
        for (CyNode node : cyNetwork.getNodeList()) {
            if (cyNetwork.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
                selectedNodes.add(node);
            }
        }
        return selectedNodes;
    }

    public List<Node> getINodes() {
        return new ArrayList<>(iNodes);
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
}
