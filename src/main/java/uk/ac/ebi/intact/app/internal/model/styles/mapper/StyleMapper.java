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
import uk.ac.ebi.intact.app.internal.ui.components.legend.NodeColorLegendEditor;
import uk.ac.ebi.intact.app.internal.utils.TimeUtils;

import java.awt.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions.Taxons.ARTIFICIAL;

public class StyleMapper {
    private static boolean taxIdsReady = false;
    private static boolean taxIdsWorking = false;
    private static boolean nodeTypesReady = false;
    private static boolean nodeTypesWorking = false;
    private static boolean edgeTypesReady = false;
    private static boolean edgeTypesWorking = false;
    private static final Vector<WeakReference<StyleUpdatedListener>> styleUpdatedListeners = new Vector<>();
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    public static Hashtable<String, Paint> speciesColors = Arrays.stream(Taxons.values())
            .filter(taxons -> taxons.isSpecies)
            .collect(toMap(
                    taxon -> taxon.taxId,
                    taxon -> taxon.defaultColor,
                    (u, v) -> u,
                    Hashtable::new)
            );

    public static Hashtable<String, Paint> kingdomColors = Arrays.stream(Taxons.values())
            .filter(taxons -> !taxons.isSpecies)
            .collect(toMap(
                    taxon -> taxon.taxId,
                    taxon -> taxon.defaultColor,
                    (u, v) -> u,
                    Hashtable::new)
            );

    public static Hashtable<String, Paint> originalSpeciesColors = new Hashtable<>(speciesColors);
    public static Hashtable<String, Paint> originalKingdomColors = new Hashtable<>(kingdomColors);

    public static Hashtable<String, List<String>> taxIdToChildrenTaxIds = new Hashtable<>();
    public static Hashtable<String, String> taxIdToParentTaxId = new Hashtable<>();

    public static final Hashtable<String, Paint> edgeTypeToPaint = Arrays.stream(InteractionType.values())
            .collect(toMap(
                    type -> type.name,
                    type -> type.defaultColor,
                    (u, v) -> u,
                    Hashtable::new)
            );

    public static final Hashtable<String, NodeShape> nodeTypeToShape = Arrays.stream(InteractorType.values())
            .collect(toMap(
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

    public static void initializeSpeciesAndKingdomColors(boolean async) {
        Runnable initColors = () -> {
            if (!taxIdsWorking) {
                taxIdsWorking = true;

                for (String kingdomId : kingdomColors.keySet()) {
                    taxIdToChildrenTaxIds.put(kingdomId, new ArrayList<>());
                }
                taxIdToChildrenTaxIds.get(ARTIFICIAL.taxId).add("-1");
                speciesColors.put("-1", kingdomColors.get(ARTIFICIAL.taxId));

                for (String parentSpecie : new ArrayList<>(speciesColors.keySet())) {
                    Paint paint = speciesColors.get(parentSpecie);
                    addDescendantsColors(parentSpecie, (Color) paint);
                }

                fireStyleUpdated();
                taxIdsReady = true;
            }
        };
        if (async) executor.execute(initColors);
        else initColors.run();
    }

    public static void addDescendantsColors(String parentSpecie, Color paint) {
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
                            String obo_id = objNode.get("obo_id").asText();
                            String id = obo_id.substring(obo_id.indexOf(":") + 1);
                            speciesColors.put(id, paint);
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

    public static void resetMappings(boolean async) {
        speciesColors = new Hashtable<>(originalSpeciesColors);
        kingdomColors = new Hashtable<>(originalKingdomColors);
        taxIdsReady = false;
        taxIdsWorking = false;
        initializeSpeciesAndKingdomColors(async);
    }

    public synchronized static Map<String, Paint> harvestKingdomsOf(Set<String> taxIdsToCheckAndAdd, boolean setColor) {
        Map<String, Paint> addedTaxIds = new Hashtable<>();

        while (!taxIdsReady)
            TimeUtils.sleep(500);

        if (taxIdsToCheckAndAdd == null)
            return addedTaxIds;

        taxIdsToCheckAndAdd.removeAll(speciesColors.keySet());
        taxIdsToCheckAndAdd.removeAll(kingdomColors.keySet());

        if (taxIdsToCheckAndAdd.size() == 0)
            return addedTaxIds;

        try {
            String resultText = HttpUtils.getRequestResultForUrl("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id=" + concatenateTaxIds(taxIdsToCheckAndAdd));
            JSONObject jObject = XML.toJSONObject(resultText);
            System.out.println(jObject);
            JsonNode taxons = new ObjectMapper().readTree(jObject.toString()).get("TaxaSet").get("Taxon");
            if (taxons.isArray()) {
                for (JsonNode taxon : taxons) {
                    addTaxon(addedTaxIds, taxon, setColor);
                }
            } else {
                addTaxon(addedTaxIds, taxons, setColor);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        fireStyleUpdated();
        return addedTaxIds;
    }

    private static void addTaxon(Map<String, Paint> addedTaxIds, JsonNode taxons, boolean setColor) {
        String taxId = taxons.get("TaxId").asText();
        ArrayNode lineage = (ArrayNode) taxons.get("LineageEx").get("Taxon");

        for (int i = lineage.size() - 1; i >= 0; i--) {
            String supTaxId = lineage.get(i).get("TaxId").asText();
            if (originalKingdomColors.containsKey(supTaxId)) {
                taxIdToChildrenTaxIds.get(supTaxId).add(taxId);
                taxIdToParentTaxId.put(taxId, supTaxId);
                if (setColor) {
                    Paint paint = kingdomColors.get(supTaxId);
                    kingdomColors.put(taxId, paint);
                    addedTaxIds.put(taxId, paint);
                }
                break;
            }
        }
    }

    public static Paint getKingdomColor(String taxId) {
        String kingdomId = taxIdToParentTaxId.get(taxId);
        if (kingdomId != null) return kingdomColors.get(kingdomId);
        Set<String> taxIdSet = new HashSet<>();
        return harvestKingdomsOf(taxIdSet, true).get(taxId);
    }

    public static Map<String, Paint> updateChildrenColors(String parentTaxId, Color newColor, boolean addDescendants, boolean isKingdom) {
        Map<String, Paint> updatedTaxIds = new Hashtable<>();

        var workingColors = isKingdom ? kingdomColors : speciesColors;
        workingColors.put(parentTaxId, newColor);
        updatedTaxIds.put(parentTaxId, newColor);

        if (addDescendants) {
            if (!isKingdom && !taxIdToChildrenTaxIds.containsKey(parentTaxId))
                addDescendantsColors(parentTaxId, newColor);

            Set<String> userDefinedTaxId = NodeColorLegendEditor.getDefinedTaxIds();

            for (String subTaxId : taxIdToChildrenTaxIds.get(parentTaxId)) {
                if (!userDefinedTaxId.contains(subTaxId)) {
                    workingColors.put(subTaxId, newColor);
                    updatedTaxIds.put(subTaxId, newColor);
                }
            }
        }
        fireStyleUpdated();
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
                        .filter(type -> type.queryChildren)
                        .forEach(type -> setChildrenValues(edgeTypeToPaint, type.name, type.defaultColor, edgeTypeToParent));

                edgeTypeToPaint.putAll(originalColors);

                edgeTypesReady = true;
            }
        });
    }

    private static <T> void setChildrenValues(Map<String, T> mapToFill, String parentLabel, T parentValue, Map<String, List<String>> parentToChildLabelMap) {
        String id = typesToIds.get(parentLabel);
        if (id == null) return;
        String jsonQuery = SourceOntology.MI.getDescendantsURL(id);

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

    private static String concatenateTaxIds(Set<String> taxIds) {
        return taxIds.stream().filter(taxId -> !taxId.startsWith("-")).collect(toList()).toString().replaceAll("[\\[\\]]", "").replaceAll(" ", "%20");
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
            ListIterator<WeakReference<StyleUpdatedListener>> listenerIterator = styleUpdatedListeners.listIterator();
            while (listenerIterator.hasNext()) {
                StyleUpdatedListener listener = listenerIterator.next().get();
                if (listener == null) listenerIterator.remove();
                else {
                    try {
                        listener.handleStyleUpdatedEvent();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
