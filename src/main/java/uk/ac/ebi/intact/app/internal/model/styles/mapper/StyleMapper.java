package uk.ac.ebi.intact.app.internal.model.styles.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.json.JSONObject;
import org.json.XML;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.events.StyleUpdatedListener;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions.InteractionType;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions.InteractorType;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions.Taxons;
import uk.ac.ebi.intact.app.internal.utils.TimeUtils;

import java.awt.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions.Taxons.ARTIFICIAL;

public class StyleMapper {
    private static boolean taxIdsReady = false;
    private static boolean taxIdsWorking = false;
    private static boolean nodeTypesReady = false;
    private static boolean nodeTypesWorking = false;
    private static boolean edgeTypesReady = false;
    private static boolean edgeTypesWorking = false;
    private static final List<WeakReference<StyleUpdatedListener>> styleUpdatedListeners = new ArrayList<>();
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    public static Hashtable<Long, Paint> taxIdToPaint = Arrays.stream(Taxons.values())
            .filter(taxons -> taxons.isSpecies)
            .collect(Collectors.toMap(
                    taxon -> taxon.taxId,
                    taxon -> taxon.defaultColor,
                    (u, v) -> u,
                    Hashtable::new)
            );

    public static Hashtable<Long, Paint> kingdomColors = Arrays.stream(Taxons.values())
            .filter(taxons -> !taxons.isSpecies)
            .collect(Collectors.toMap(
                    taxon -> taxon.taxId,
                    taxon -> taxon.defaultColor,
                    (u, v) -> u,
                    Hashtable::new)
            );

    public static Hashtable<Long, Paint> originalTaxIdToPaint = new Hashtable<>(taxIdToPaint);
    public static Hashtable<Long, Paint> originalKingdomColors = new Hashtable<>(kingdomColors);

    public static Hashtable<Long, List<Long>> taxIdToChildrenTaxIds = new Hashtable<>();

    public static final Hashtable<String, Paint> edgeTypeToPaint = Arrays.stream(InteractionType.values())
            .collect(Collectors.toMap(
                    type -> type.name,
                    type -> type.defaultColor,
                    (u, v) -> u,
                    Hashtable::new)
            );

    public static final Hashtable<String, NodeShape> nodeTypeToShape = Arrays.stream(InteractorType.values())
            .collect(Collectors.toMap(
                    type -> type.name,
                    type -> type.shape,
                    (u, v) -> u,
                    Hashtable::new)
            );

    public static final Hashtable<String, NodeShape> originalNodeTypeToShape = new Hashtable<>(nodeTypeToShape);

    public static final Hashtable<String, List<String>> nodeTypeToParent = new Hashtable<>();
    public static final Hashtable<String, List<String>> edgeTypeToParent = new Hashtable<>();

    public static final Map<String, String> typesToIds = new HashMap<>();

    static {
        Arrays.stream(InteractionType.values()).filter(type -> !type.MI_ID.isBlank()).forEach(type -> typesToIds.put(type.name, type.MI_ID));
        Arrays.stream(InteractorType.values()).filter(type -> !type.MI_ID.isBlank()).forEach(type -> typesToIds.put(type.name, type.MI_ID));
    }

    public static void initializeTaxIdToPaint() {
        executor.execute(() -> {
            if (!taxIdsWorking) {
                taxIdsWorking = true;

                for (Long kingdomId : kingdomColors.keySet()) {
                    taxIdToChildrenTaxIds.put(kingdomId, new ArrayList<>());
                }
                taxIdToChildrenTaxIds.get(ARTIFICIAL.taxId).add(-1L);
                taxIdToPaint.put(-1L, kingdomColors.get(ARTIFICIAL.taxId));

                for (Long parentSpecie : new ArrayList<>(taxIdToPaint.keySet())) {
                    Paint paint = taxIdToPaint.get(parentSpecie);
                    addDescendantsColors(parentSpecie, (Color) paint);
                }

                fireStyleUpdated();
                taxIdsReady = true;
            }
        });
    }

    public static void addDescendantsColors(Long parentSpecie, Color paint) {
        taxIdToChildrenTaxIds.put(parentSpecie, new ArrayList<>());

        String jsonQuery = "https://www.ebi.ac.uk/ols/api/ontologies/ncbitaxon/terms/" +
                "http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCBITaxon_" + parentSpecie + "/descendants?size=1000";

        try {
            boolean hasNext = true;
            while (hasNext) {
                JsonNode json = HttpUtils.getJsonForUrl(jsonQuery);
                if (json != null) {
                    if (json.get("page").get("totalElements").intValue() > 0) {

                        JsonNode termChildren = json.get("_embedded").get("terms");

                        for (final JsonNode objNode : termChildren) {
                            String obo_id = objNode.get("obo_id").textValue();
                            Long id = Long.parseLong(obo_id.substring(obo_id.indexOf(":") + 1));
                            taxIdToPaint.put(id, paint);
                            taxIdToChildrenTaxIds.get(parentSpecie).add(id);
                        }
                    }
                    JsonNode nextPage = json.get("_links").get("next");
                    if (nextPage != null) {
                        jsonQuery = nextPage.get("href").textValue();
                    } else {
                        hasNext = false;
                    }
                } else {
                    hasNext = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetMappings() {
        taxIdToPaint = new Hashtable<>(originalTaxIdToPaint);
        kingdomColors = new Hashtable<>(originalKingdomColors);
        taxIdsReady = false;
        taxIdsWorking = false;
        initializeTaxIdToPaint();
    }

    public synchronized static Map<Long, Paint> completeTaxIdColorsFromUnknownTaxIds(Set<Long> taxIdsToCheckAndAdd) {
        Map<Long, Paint> addedTaxIds = new Hashtable<>();

        while (!taxIdsReady)
            TimeUtils.sleep(500);

        if (taxIdsToCheckAndAdd == null)
            return addedTaxIds;

        taxIdsToCheckAndAdd.removeAll(taxIdToPaint.keySet());

        if (taxIdsToCheckAndAdd.size() == 0)
            return addedTaxIds;

        try {
            String resultText = HttpUtils.getRequestResultForUrl("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id=" + concatenateTaxIds(taxIdsToCheckAndAdd));
            JSONObject jObject = XML.toJSONObject(resultText);
            JsonNode taxons = new ObjectMapper().readTree(jObject.toString()).get("TaxaSet").get("Taxon");
            if (taxons.isArray()) {
                for (JsonNode taxon : taxons) {
                    addTaxon(addedTaxIds, taxon);
                }
            } else {
                addTaxon(addedTaxIds, taxons);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        fireStyleUpdated();
        return addedTaxIds;
    }

    private static void addTaxon(Map<Long, Paint> addedTaxIds, JsonNode taxons) {
        Long taxId = taxons.get("TaxId").longValue();
        ArrayNode lineage = (ArrayNode) taxons.get("LineageEx").get("Taxon");

        for (int i = lineage.size() - 1; i >= 0; i--) {
            Long supTaxId = lineage.get(i).get("TaxId").longValue();
            if (originalKingdomColors.containsKey(supTaxId)) {
                Paint paint = originalKingdomColors.get(supTaxId);
                kingdomColors.put(taxId, paint);
                taxIdToChildrenTaxIds.get(supTaxId).add(taxId);
                addedTaxIds.put(taxId, paint);
                break;
            }
        }
    }

    public static Map<Long, Paint> updateChildrenColors(Long parentTaxId, Color newColor, boolean addDescendants) {
        Map<Long, Paint> updatedTaxIds = new Hashtable<>();

        taxIdToPaint.put(parentTaxId, newColor);
        updatedTaxIds.put(parentTaxId, newColor);

        if (addDescendants) {
            if (!taxIdToChildrenTaxIds.containsKey(parentTaxId))
                addDescendantsColors(parentTaxId, newColor);

            for (Long subTaxId : taxIdToChildrenTaxIds.get(parentTaxId)) {
                taxIdToPaint.put(subTaxId, newColor);
                updatedTaxIds.put(subTaxId, newColor);
            }
        }
        return updatedTaxIds;
    }

    public static void initializeNodeTypeToShape() {
        executor.execute(() -> {
            if (!nodeTypesWorking) {
                nodeTypesWorking = true;
                for (String miType : new ArrayList<>(nodeTypeToShape.keySet())) {
                    setChildrenValues(nodeTypeToShape, miType, nodeTypeToShape.get(miType), nodeTypeToParent);
                }
                nodeTypesReady = true;
            }
        });
    }

    public static void initializeEdgeTypeToPaint() {
        executor.execute(() -> {
            if (!edgeTypesWorking) {
                edgeTypesWorking = true;

                Map<String, Paint> originalColors = new Hashtable<>(edgeTypeToPaint);

                Arrays.stream(InteractionType.values())
                        .filter(type-> type.queryChildren)
                        .forEach(type -> setChildrenValues(edgeTypeToPaint, type.name, type.defaultColor, edgeTypeToParent));

                edgeTypeToPaint.putAll(originalColors);

                edgeTypesReady = true;
            }
        });
    }

    private static <T> void setChildrenValues(Map<String, T> mapToFill, String parentLabel, T parentValue, Map<String, List<String>> parentToChildLabelMap) {
        String jsonQuery = SourceOntology.MI.getDescendantsURL(typesToIds.get(parentLabel));

        try {
            boolean hasNext = true;
            while (hasNext) {
                JsonNode json = HttpUtils.getJsonForUrl(jsonQuery);
                if (json != null) {
                    if (json.get("page").get("totalElements").intValue() > 0) {

                        JsonNode termChildren = json.get("_embedded").get("terms");

                        List<String> children = new ArrayList<>();
                        for (final JsonNode objNode : termChildren) {
                            String label = objNode.get("label").textValue();
                            mapToFill.put(label, parentValue);
                            mapToFill.put(label.replaceAll(" reaction", ""), parentValue);
                            children.add(label);
                        }
                        parentToChildLabelMap.put(parentLabel, children);
                    }
                    JsonNode nextPage = json.get("_links").get("next");
                    if (nextPage != null) {
                        jsonQuery = nextPage.get("href").textValue();
                    } else {
                        hasNext = false;
                    }
                } else {
                    hasNext = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String concatenateTaxIds(Set<Long> taxIds) {
        return taxIds.stream().filter(taxId -> taxId > 0).collect(Collectors.toList()).toString().replaceAll("[\\[\\]]", "").replaceAll(" ", "%20");
    }

    public static boolean speciesNotReady() {
        return !taxIdsReady;
    }

    public static boolean nodeTypesNotReady() {
        return !nodeTypesReady;
    }

    public static boolean edgeTypesNotReady() {
        return !edgeTypesReady;
    }

    public static void fireStyleUpdated() {
        executor.execute(() -> {
            Iterator<WeakReference<StyleUpdatedListener>> listenerIterator = styleUpdatedListeners.iterator();
            while (listenerIterator.hasNext()) {
                StyleUpdatedListener listener = listenerIterator.next().get();
                if (listener == null) listenerIterator.remove();
                else listener.handleStyleUpdatedEvent();
            }
        });
    }


    public static void addStyleUpdatedListener(StyleUpdatedListener listener) {
        styleUpdatedListeners.add(new WeakReference<>(listener));
    }

    public static void removeStyleUpdatedListener(StyleUpdatedListener listener) {
        styleUpdatedListeners.removeIf(ref -> ref.get() == null || ref.get() == listener);
    }
}
