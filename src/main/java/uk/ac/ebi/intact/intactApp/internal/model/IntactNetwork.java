package uk.ac.ebi.intact.intactApp.internal.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.model.*;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.styles.IntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TableUtil;

import java.awt.*;
import java.util.List;
import java.util.*;

import static uk.ac.ebi.intact.intactApp.internal.utils.TableUtil.getColumnValuesOfEdges;

// import org.jcolorbrewer.ColorBrewer;

public class IntactNetwork implements AddedEdgesListener, AboutToRemoveEdgesListener {
    final IntactManager manager;
    CyNetwork network;
    Map<String, List<String>> resolvedIdMap;
    Map<String, List<Annotation>> annotations;
    CyTable edgeTable;
    CyTable nodeTable;
    CyTable featuresTable;
    CyTable identifiersTable;


    // Collapsed edges
    private Map<Couple, CyEdge> collapsedEdges;
    private List<CyEdge> expandedEdges;
    private Map<Couple, List<CyEdge>> coupleToEdges = new HashMap<>();
    private final Set<Long> taxIds = new HashSet<>();
    private final Set<String> interactorTypes = new HashSet<>();
    private final Map<String, Long> speciesNameToId = new HashMap<>();
    private final Map<Long, String> speciesIdToName = new HashMap<>();
    private boolean styleCompleted = false;


    public IntactNetwork(IntactManager manager) {
        this.manager = manager;
        resolvedIdMap = null;
        annotations = null;
    }

    public void reset() {
        resolvedIdMap = null;
        annotations = null;
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

        TableUtil.NullAndNonNullEdges identifiedOrNotEdges = TableUtil.splitNullAndNonNullEdges(network, ModelUtils.INTACT_AC);

        expandedEdges = new ArrayList<>(identifiedOrNotEdges.nonNullEdges);
        collapsedEdges = new HashMap<>();
        coupleToEdges = new HashMap<>();

        Couple.putEdgesToCouples(expandedEdges, coupleToEdges);

        if (identifiedOrNotEdges.nullEdges.size() > 0) {
            for (CyEdge existingEdge : identifiedOrNotEdges.nullEdges) {
                Couple existingCouple = new Couple(existingEdge);
                collapsedEdges.put(existingCouple, existingEdge);
            }
        } else {
            updateCollapsedEdges(coupleToEdges.keySet());
        }

        completeMissingNodeColors();
    }

    void hideExpandedEdgesOnViewCreation(CyNetworkView networkView) {
        HideTaskFactory hideTaskFactory = manager.getService(HideTaskFactory.class);
        manager.execute(hideTaskFactory.createTaskIterator(networkView, null, expandedEdges));
        manager.addNetworkView(networkView);
    }

    public void completeMissingNodeColors() {
        new Thread(() -> {
            for (CyRow row : nodeTable.getAllRows()) {
                interactorTypes.add(row.get(ModelUtils.TYPE, String.class));
                Long taxId = row.get(ModelUtils.TAX_ID, Long.class);
                taxIds.add(taxId);
                String specieName = row.get(ModelUtils.SPECIES, String.class);
                speciesNameToId.put(specieName, taxId);
                speciesIdToName.put(taxId, specieName);
            }

            Map<Long, Paint> addedTaxIds = StyleMapper.completeTaxIdColorsFromUnknownTaxIds(getTaxIds());

            for (IntactStyle style : manager.getIntactStyles().values()) {
                style.updateTaxIdToNodePaintMapping(addedTaxIds);
            }
            styleCompleted = true;
        }).start();
    }

    public boolean isStyleCompleted() {
        return styleCompleted;
    }

    public Set<String> getInteractorTypes() {
        return interactorTypes;
    }

    public Set<Long> getTaxIds() {
        return new HashSet<>(taxIds);
    }

    public String getSpeciesName(Long taxId) {
        return speciesIdToName.getOrDefault(taxId, null);
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


    public Map<String, List<Annotation>> getAnnotations() {
        return annotations;
    }

    public Map<String, List<Annotation>> getAnnotations(int taxon, final String terms,
                                                        final String useDATABASE, boolean includeViruses) {
        String encTerms = terms.trim();

        // Split the terms up into groups of 5000
        String[] termsArray = encTerms.split("\n");
        annotations = new HashMap<>();
        for (int i = 0; i < termsArray.length; i = i + 5000) {
            String termsBatch = getTerms(termsArray, i, i + 5000, termsArray.length);
            annotations = getAnnotationBatch(taxon, termsBatch, useDATABASE, includeViruses);
        }

//        IntactImporter imp = new IntactImporter(this, manager);
        return annotations;
    }

    private Map<String, List<Annotation>> getAnnotationBatch(int taxon, final String encTerms,
                                                             String useDATABASE, boolean includeViruses) {
        String url = manager.getResolveURL(Databases.STRING.getAPIName()) + "json/get_string_ids";
        Map<String, String> args = new HashMap<>();
        args.put("species", Integer.toString(taxon));
        args.put("identifiers", encTerms);
        args.put("caller_identity", IntactManager.CallerIdentity);
        manager.info("URL: " + url + "?species=" + taxon + "&caller_identity=" + IntactManager.CallerIdentity + "&identifiers=" + encTerms);
        JsonNode results = HttpUtils.getJSON(url, args, manager);
        System.out.println(results);

        if (results != null) {
            annotations = Annotation.getAnnotations(results, encTerms, annotations);
        }
        results = null;

        // then, call other APIs to get resolve them
        // resolve compounds
        if (useDATABASE.equals(Databases.STITCH.getAPIName())) {
            url = manager.getResolveURL(Databases.STITCH.getAPIName()) + "json/resolveList";
            args = new HashMap<>();
            args.put("species", "CIDm");
            args.put("identifiers", encTerms);
            args.put("caller_identity", IntactManager.CallerIdentity);
            manager.info("URL: " + url + "?species=" + taxon + "&caller_identity=" + IntactManager.CallerIdentity + "&identifiers=" + HttpUtils.truncate(encTerms));
            // Get the results
            // System.out.println("Getting STITCH term resolution");
            results = HttpUtils.getJSON(url, args, manager);

            if (results != null) {
                updateAnnotations(results, encTerms);
            }
            results = null;
        }

        // also call the viruses API
        if (manager.isVirusesEnabled() && annotations.size() == 0 && includeViruses) {
            // http://viruses.string-db.org/cgi/webservice_handler.pl?species=11320&identifiers=NS1_I34A1
            // &caller_identity=string_app_v1_1_1&output=json&request=resolveList
            url = manager.getResolveURL(Databases.VIRUSES.getAPIName());
            args = new HashMap<>();
            args.put("species", Integer.toString(taxon));
            args.put("identifiers", encTerms);
            args.put("caller_identity", IntactManager.CallerIdentity);
            args.put("output", "json");
            args.put("request", "resolveList");
            manager.info("URL:" + url + "?species=" + taxon + "&caller_identity="
                    + IntactManager.CallerIdentity + "&identifiers=" + HttpUtils.truncate(encTerms));
            // Get the results
            // System.out.println("Getting VIRUSES term resolution");
            results = HttpUtils.getJSON(url, args, manager);

            if (results != null) {
                updateAnnotations(results, encTerms);
            }
            results = null;
        }

        return annotations;
    }

    private String getTerms(String[] termsArray, int start, int end, int length) {
        if (length == 1) return termsArray[0];
        if (end > length) end = length;
        StringBuilder terms = null;
        for (int i = start; i < (end); i++) {
            if (terms == null) {
                terms = new StringBuilder();
                terms.append(termsArray[i]);
            } else {
                terms.append("\n").append(termsArray[i]);
            }
        }
        return terms.toString();
    }


    /*
     * Maintenance of the resolveIdMap
     */
    public boolean resolveAnnotations() {
        if (resolvedIdMap == null) resolvedIdMap = new HashMap<>();
        boolean noAmbiguity = true;
        for (String key : annotations.keySet()) {
            if (annotations.get(key).size() > 1) {
                noAmbiguity = false;
                break;
            } else {
                List<String> ids = new ArrayList<>();
                ids.add(annotations.get(key).get(0).getStringId());
                resolvedIdMap.put(key, ids);
            }
        }

        // Now trim the key set
        if (resolvedIdMap.size() > 0) {
            for (String key : resolvedIdMap.keySet()) {
                annotations.remove(key);
            }
        }
        return noAmbiguity;
    }

    public void addResolvedStringID(String term, String id) {
        if (!resolvedIdMap.containsKey(term))
            resolvedIdMap.put(term, new ArrayList<>());
        resolvedIdMap.get(term).add(id);
    }

    public void removeResolvedStringID(String term, String id) {
        if (!resolvedIdMap.containsKey(term))
            return;
        List<String> ids = resolvedIdMap.get(term);
        ids.remove(id);
        if (ids.size() == 0)
            resolvedIdMap.remove(term);
    }

    public boolean haveResolvedNames() {
        // allows users to not resolve some of the proteins but still needs at least one protein as input
        return resolvedIdMap == null || resolvedIdMap.size() > 0;
    }

    public int getResolvedTerms() {
        int i = 0;
        for (List<String> terms : resolvedIdMap.values())
            i += terms.size();
        return i;
    }

    public List<String> combineIds(Map<String, String> reverseMap) {
        List<String> ids = new ArrayList<>();
        for (String term : resolvedIdMap.keySet()) {
            for (String id : resolvedIdMap.get(term)) {
                ids.add(id);
                reverseMap.put(id, term);
            }
        }
        return ids;
    }

    private void updateAnnotations(JsonNode results, String terms) {
        Map<String, List<Annotation>> newAnnotations = Annotation.getAnnotations(results,
                terms);
        for (String newAnn : newAnnotations.keySet()) {
            List<Annotation> allAnn = new ArrayList<>(newAnnotations.get(newAnn));
            if (annotations.containsKey(newAnn)) {
                allAnn.addAll(annotations.get(newAnn));
            }
            annotations.put(newAnn, allAnn);
        }
    }

    // INTACT INTACT INTACT INTACT INTACT INTACT INTACT INTACT INTACT //

    private void updateCollapsedEdges(Collection<Couple> couplesToUpdate) {
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
                CyRow summaryEdgeRow = network.getRow(summaryEdge);
                summaryEdgeRow.set(ModelUtils.C_INTACT_IDS, getColumnValuesOfEdges(edgeTable, ModelUtils.INTACT_ID, Long.class, similarEdges, "???"));
                CyRow firstEdgeRow = network.getRow(similarEdges.iterator().next());
                summaryEdgeRow.set(ModelUtils.MI_SCORE, firstEdgeRow.get(ModelUtils.MI_SCORE, Double.class));
                summaryEdgeRow.set(ModelUtils.C_IS_COLLAPSED, true);
                summaryEdgeRow.set(ModelUtils.C_NB_EDGES, similarEdges.size());
                summaryEdgeRow.set(CyNetwork.NAME, ModelUtils.getName(network, couple.node1) + " (interact with) " + ModelUtils.getName(network, couple.node2));

            } else {
                summaryEdge = collapsedEdges.get(couple);
                network.removeEdges(Collections.singleton(summaryEdge));
            }
        }
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

    // INTACT INTACT INTACT INTACT INTACT INTACT INTACT INTACT INTACT //


    public CyTable getFeaturesTable() {
        return featuresTable;
    }

    public void setFeaturesTable(CyTable featuresTable) {
        this.featuresTable = featuresTable;
        if (network != null) {
            network.getRow(network).set(ModelUtils.FEATURES_TABLE_REF, featuresTable.getSUID());
        }
    }

    public CyTable getIdentifiersTable() {
        return identifiersTable;
    }

    public void setIdentifiersTable(CyTable identifiersTable) {
        this.identifiersTable = identifiersTable;
        if (network != null)
            network.getRow(network).set(ModelUtils.IDENTIFIERS_TABLE_REF, identifiersTable.getSUID());
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
}
