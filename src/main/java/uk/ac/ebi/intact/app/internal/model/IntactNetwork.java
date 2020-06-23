package uk.ac.ebi.intact.app.internal.model;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.*;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.IntactNode;
import uk.ac.ebi.intact.app.internal.model.core.Interactor;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.app.internal.model.styles.IntactStyle;
import uk.ac.ebi.intact.app.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.app.internal.utils.TableUtil;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.*;

public class IntactNetwork implements AddedEdgesListener, AboutToRemoveEdgesListener {
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    final IntactManager manager;
    CyNetwork network;
    Map<String, List<String>> termToAcs;
    Map<String, List<Interactor>> interactorsToResolve;
    CyTable edgeTable;
    CyTable nodeTable;
    CyTable featuresTable;
    CyTable identifiersTable;

    private final List<IntactNode> iNodes = new ArrayList<>();
    // Collapsed edges
    private final Map<Couple, List<CyEdge>> coupleToEdges = new HashMap<>();
    private final Map<Couple, CyEdge> collapsedEdges = new HashMap<>();
    private final List<IntactCollapsedEdge> collapsedIEdges = new ArrayList<>();
    private final List<CyEdge> expandedEdges = new ArrayList<>();
    private final List<IntactEvidenceEdge> evidenceIEdges = new ArrayList<>();

    private final Set<Long> taxIds = new HashSet<>();
    private final Set<String> interactorTypes = new HashSet<>();
    private final Map<String, Long> speciesNameToId = new HashMap<>();
    private final Map<Long, String> speciesIdToName = new HashMap<>();


    public IntactNetwork(IntactManager manager) {
        this.manager = manager;
        termToAcs = null;
        interactorsToResolve = null;
    }

    public IntactManager getManager() {
        return manager;
    }

    public CyNetwork getNetwork() {
        return network;
    }

    public void setNetwork(CyNetwork network) {
        this.network = network;

        edgeTable = network.getDefaultEdgeTable();
        nodeTable = network.getDefaultNodeTable();

        TableUtil.NullAndNonNullEdges identifiedOrNotEdges = TableUtil.splitNullAndNonNullEdges(network, INTACT_AC);

        expandedEdges.addAll(identifiedOrNotEdges.nonNullEdges);

        Couple.putEdgesToCouples(expandedEdges, coupleToEdges);

        if (identifiedOrNotEdges.nullEdges.size() > 0) {
            for (CyEdge existingEdge : identifiedOrNotEdges.nullEdges) {
                Couple existingCouple = new Couple(existingEdge);
                collapsedEdges.put(existingCouple, existingEdge);
            }
        } else {
            updateCollapsedEdges(coupleToEdges.keySet());
        }

        network.getNodeList().forEach(node -> iNodes.add(new IntactNode(this, node)));
        collapsedEdges.values().forEach(edge -> collapsedIEdges.add((IntactCollapsedEdge) IntactEdge.createIntactEdge(this, edge)));
        expandedEdges.forEach(edge -> evidenceIEdges.add((IntactEvidenceEdge) IntactEdge.createIntactEdge(this, edge)));

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


    public Map<String, List<Interactor>> getInteractorsToResolve() {
        return interactorsToResolve;
    }

    public Map<String, List<Interactor>> resolveTerms(final String terms, boolean exactQuery) {
        Map<Object, Object> resolverData = new HashMap<>();
        resolverData.put("query", terms.replaceAll("[\\n\\s\\r]", " "));
        resolverData.put("fuzzySearch", !exactQuery);
        resolverData.put("pageSize", manager.option.MAX_INTERACTOR_PER_TERM.getValue());
        interactorsToResolve = Interactor.getInteractorsToResolve(HttpUtils.postJSON(IntactManager.INTACT_INTERACTOR_WS + "list/resolve", resolverData, manager));
        completeMissingNodeColorsFromInteractors();
        return interactorsToResolve;
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
        manager.utils.execute(hideTaskFactory.createTaskIterator(networkView, null, expandedEdges));
        manager.data.addNetworkView(networkView, false);
    }

    public void completeMissingNodeColorsFromTables() {
        executor.execute(() -> {
            for (CyRow row : nodeTable.getAllRows()) {
                interactorTypes.add(row.get(TYPE, String.class));
                Long taxId = row.get(TAX_ID, Long.class);
                taxIds.add(taxId);
                String specieName = row.get(SPECIES, String.class);
                speciesNameToId.put(specieName, taxId);
                speciesIdToName.put(taxId, specieName);
            }

            Map<Long, Paint> addedTaxIds = StyleMapper.completeTaxIdColorsFromUnknownTaxIds(getTaxIds());

            for (IntactStyle style : manager.style.getIntactStyles().values()) {
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

            for (IntactStyle style : manager.style.getIntactStyles().values()) {
                style.updateTaxIdToNodePaintMapping(addedTaxIds);
            }
        });
    }

    private void updateCollapsedEdges(Collection<Couple> couplesToUpdate) {
        CyEventHelper eventHelper = manager.utils.getService(CyEventHelper.class);
        CyTable table = network.getDefaultEdgeTable();
        eventHelper.silenceEventSource(table);
        for (Couple couple : couplesToUpdate) {
            CyEdge summaryEdge;
            List<CyEdge> similarEdges = coupleToEdges.get(couple);
            if (!similarEdges.isEmpty()) {
                if (!collapsedEdges.containsKey(couple)) {
                    summaryEdge = network.addEdge(couple.node1, couple.node2, false);
                    collapsedEdges.put(couple, summaryEdge);
                } else {
                    summaryEdge = collapsedEdges.get(couple);
                }
                CyRow summaryEdgeRow = table.getRow(summaryEdge.getSUID());


                List<String> sourceFeatures = new ArrayList<>();
                List<String> targetFeatures = new ArrayList<>();
                for (CyEdge edge : similarEdges) {
                    CyRow edgeRow = edgeTable.getRow(edge.getSUID());

                    List<String> edgeSourceFeatures = edgeRow.getList(SOURCE_FEATURES, String.class);
                    List<String> edgeTargetFeatures = edgeRow.getList(TARGET_FEATURES, String.class);
                    if (edge.getSource().equals(couple.node1)) {
                        if (edgeSourceFeatures != null) sourceFeatures.addAll(edgeSourceFeatures);
                        if (edgeTargetFeatures != null) targetFeatures.addAll(edgeTargetFeatures);
                    } else {
                        if (edgeTargetFeatures != null) sourceFeatures.addAll(edgeTargetFeatures);
                        if (edgeSourceFeatures != null) targetFeatures.addAll(edgeSourceFeatures);
                    }
                }
                summaryEdgeRow.set(SOURCE_FEATURES, sourceFeatures);
                summaryEdgeRow.set(TARGET_FEATURES, targetFeatures);

                summaryEdgeRow.set(C_INTACT_IDS, TableUtil.getColumnValuesOfEdges(edgeTable, INTACT_ID, Long.class, similarEdges, "???"));
                summaryEdgeRow.set(C_INTACT_SUIDS, TableUtil.getColumnValuesOfEdges(edgeTable, CyEdge.SUID, Long.class, similarEdges, "???"));
                CyRow firstEdgeRow = network.getRow(similarEdges.get(0));
                summaryEdgeRow.set(MI_SCORE, firstEdgeRow.get(MI_SCORE, Double.class));
                summaryEdgeRow.set(C_IS_COLLAPSED, true);
                summaryEdgeRow.set(C_NB_EDGES, similarEdges.size());
                summaryEdgeRow.set(CyNetwork.NAME, getName(network, couple.node1) + " (interact with) " + getName(network, couple.node2));

            } else {
                summaryEdge = collapsedEdges.get(couple);
                network.removeEdges(Collections.singleton(summaryEdge));
            }
        }
        eventHelper.unsilenceEventSource(table);

    }

    public List<IntactCollapsedEdge> getCollapsedIEdges() {
        return new ArrayList<>(collapsedIEdges);
    }

    public List<IntactEvidenceEdge> getEvidenceIEdges() {
        return new ArrayList<>(evidenceIEdges);
    }

    public List<CyEdge> getCollapsedEdges() {
        return new ArrayList<>(collapsedEdges.values());
    }

    public List<CyEdge> getExpandedEdges() {
        return new ArrayList<>(expandedEdges);
    }

    public List<CyEdge> getEvidenceEdges(CyEdge edge) {
        return coupleToEdges.get(new Couple(edge));
    }

    public CyEdge getCollapsedEdge(CyEdge edge) {
        return collapsedEdges.get(new Couple(edge));
    }


    @Override
    public void handleEvent(AddedEdgesEvent e) {
        if (e.getSource() == network) {
            Collection<CyEdge> addedEdges = e.getPayloadCollection();
            expandedEdges.addAll(addedEdges);
//            expandedIEdges.addAll(addedEdges.stream().map(edge -> (IntactEvidenceEdge) IntactEdge.createIntactEdge(this, edge)).filter(Objects::nonNull).collect(Collectors.toList()));
            Set<Couple> updatedCouples = Couple.putEdgesToCouples(addedEdges, coupleToEdges);
            updateCollapsedEdges(updatedCouples);
        }
    }


    @Override
    public void handleEvent(AboutToRemoveEdgesEvent e) {
        if (e.getSource() == network) {
            Collection<CyEdge> removedEdges = e.getEdges();
            expandedEdges.removeAll(removedEdges);
            Map<Couple, List<CyEdge>> couplesToRemove = new HashMap<>();
            Couple.putEdgesToCouples(removedEdges, couplesToRemove);
            couplesToRemove.forEach((couple, cyEdges) -> coupleToEdges.get(couple).removeAll(cyEdges));
            updateCollapsedEdges(couplesToRemove.keySet());
        }
    }


    public CyTable getFeaturesTable() {
        return featuresTable;
    }

    public void setFeaturesTable(CyTable featuresTable) {
        this.featuresTable = featuresTable;
        if (network != null) {
            network.getRow(network).set(NET_FEATURES_TABLE_REF, featuresTable.getSUID());
        }
    }

    public CyTable getIdentifiersTable() {
        return identifiersTable;
    }

    public void setIdentifiersTable(CyTable identifiersTable) {
        this.identifiersTable = identifiersTable;
        if (network != null)
            network.getRow(network).set(NET_IDENTIFIERS_TABLE_REF, identifiersTable.getSUID());
    }

    public List<CyEdge> getSelectedEdges() {
        List<CyEdge> selectedEdges = new ArrayList<>();
        for (CyEdge edge : network.getEdgeList()) {
            if (network.getRow(edge).get(CyNetwork.SELECTED, Boolean.class)) {
                selectedEdges.add(edge);
            }
        }
        return selectedEdges;
    }

    public List<CyNode> getSelectedNodes() {
        List<CyNode> selectedNodes = new ArrayList<>();
        for (CyNode node : network.getNodeList()) {
            if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
                selectedNodes.add(node);
            }
        }
        return selectedNodes;
    }

    public List<IntactNode> getINodes() {
        return new ArrayList<>(iNodes);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntactNetwork that = (IntactNetwork) o;

        return network.equals(that.network);
    }

    @Override
    public int hashCode() {
        return network.hashCode();
    }


    @Override
    public String toString() {
        return network.getRow(network).get(CyNetwork.NAME, String.class);
    }
}
