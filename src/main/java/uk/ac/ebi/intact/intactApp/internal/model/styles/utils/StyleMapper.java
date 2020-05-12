package uk.ac.ebi.intact.intactApp.internal.model.styles.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.json.JSONObject;
import org.json.XML;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TimeUtils;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.intactApp.internal.io.HttpUtils.getRequestResultForUrl;
import static uk.ac.ebi.intact.intactApp.internal.model.styles.utils.Taxons.*;

public class StyleMapper {
    private static boolean taxIdsReady = false;
    private static boolean taxIdsWorking = false;
    private static boolean nodeTypesReady = false;
    private static boolean nodeTypesWorking = false;
    private static boolean edgeTypesReady = false;
    private static boolean edgeTypesWorking = false;

    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
    public static Hashtable<Long, Paint> taxIdToPaint = new Hashtable<>() {{
        put(E_COLI.taxId, new Color(137, 51, 54)); // Escherichia coli
        put(S_CEREVISIAE.taxId, new Color(174, 125, 52));  // Saccharomyces cerevisiae
        put(H_SAPIENS.taxId, new Color(51, 94, 148));  // Homo sapiens
        put(M_MUSCULUS.taxId, new Color(28, 67, 156)); // Mus musculus
        put(D_MELANOGASTER.taxId, new Color(50, 147, 143)); // Drosophila melanogaster
        put(C_ELEGANS.taxId, new Color(74, 147, 121)); // Caenorhabditis elegans
        put(A_THALIANA.taxId, new Color(46, 93, 46));  // Arabidopsis thaliana

        put(CHEMICAL_SYNTHESIS.taxId, new Color(141, 102, 102));  // Chemical Synthesis
    }};
    public static Hashtable<Long, Paint> kingdomColors = new Hashtable<>() {{
        put(PLANTS.taxId, new Color(80, 162, 79)); // Viridiplantae (Plants)
        put(MAMMALS.taxId, new Color(86, 136, 192)); // Mammals
        put(ANIMALS.taxId, new Color(62, 181, 170)); // Metazoa (Animals)
        put(FUNGI.taxId, new Color(235, 144, 0)); // Fungi
        put(BACTERIA.taxId, new Color(178, 53, 57)); // Bacteria
        put(VIRUSES.taxId, new Color(132, 100, 190)); // Viruses
        put(ARCHAEA.taxId, new Color(172, 71, 101)); // Archaea
        put(ARTIFICIAL.taxId, new Color(101, 101, 101));
    }};

    public static Hashtable<Long, Paint> originalTaxIdToPaint = new Hashtable<>(taxIdToPaint);
    public static Hashtable<Long, Paint> originalKingdomColors = new Hashtable<>(kingdomColors);

    public static Hashtable<Long, List<Long>> taxIdToChildrenTaxIds = new Hashtable<>();

    public static final Hashtable<String, Paint> edgeTypeToPaint = new Hashtable<>() {{
        put("colocalization", new Color(165, 165, 165));
        put("association", new Color(97, 131, 196));
        put("physical association", new Color(178, 101, 188));
        put("direct interaction", new Color(184, 54, 75));
        put("phosphorylation reaction", new Color(231, 111, 61));
        put("phosphorylation", new Color(231, 111, 61));
        put("dephosphorylation", new Color(231, 111, 61));
        put("dephosphorylation reaction", new Color(231, 111, 61));
    }};

    public static final Hashtable<String, NodeShape> nodeTypeToShape = new Hashtable<>() {{
        put("bioactive entity", NodeShapeVisualProperty.TRIANGLE);
        put("protein", NodeShapeVisualProperty.ELLIPSE);
        put("gene", NodeShapeVisualProperty.ROUND_RECTANGLE);
        put("dna", BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE"));
        put("rna", NodeShapeVisualProperty.DIAMOND);
        put("complex", NodeShapeVisualProperty.HEXAGON);
        put("peptide", NodeShapeVisualProperty.OCTAGON);
    }};

    public static final Hashtable<String, NodeShape> originalNodeTypeToShape = new Hashtable<>(nodeTypeToShape);

    public static final Hashtable<String, List<String>> nodeTypeToParent = new Hashtable<>();
    public static final Hashtable<String, List<String>> edgeTypeToParent = new Hashtable<>();

    public static final Map<String, String> typesToIds = new HashMap<>() {{
        put("bioactive entity", "MI_1100");
        put("rna", "MI_0320");
        put("dna", "MI_0319");
        put("gene", "MI_0250");
        put("protein", "MI_0326");
        put("peptide", "MI_0327");

        put("direct interaction", "MI_0407");
        put("phosphorylation reaction", "MI_0217");
        put("dephosphorylation reaction", "MI_0203");
        put("association", "MI_0914");
        put("physical association", "MI_0915");
        put("colocalization", "MI_0403");
    }};

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
            String resultText = getRequestResultForUrl("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id=" + concatenateTaxIds(taxIdsToCheckAndAdd));
            JSONObject jObject = XML.toJSONObject(resultText);
            JsonNode taxons = new ObjectMapper().readTree(jObject.toString()).get("TaxaSet").get("Taxon");
            for (JsonNode taxon : taxons) {
                Long taxId = taxon.get("TaxId").longValue();
                ArrayNode lineage = (ArrayNode) taxon.get("LineageEx").get("Taxon");

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

        } catch (IOException e) {
            e.printStackTrace();
        }
        return addedTaxIds;
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

                for (String miType : List.of("direct interaction", "phosphorylation reaction", "dephosphorylation reaction")) {
                    setChildrenValues(edgeTypeToPaint, miType, originalColors.get(miType), edgeTypeToParent);
                }
                edgeTypeToPaint.putAll(originalColors);


                edgeTypesReady = true;
            }
        });
    }

    private static <T> void setChildrenValues(Map<String, T> mapToFill, String parentLabel, T parentValue, Map<String, List<String>> parentToChildLabelMap) {
        String jsonQuery = "https://www.ebi.ac.uk/ols/api/ontologies/mi/terms/" +
                "http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252F" + typesToIds.get(parentLabel) + "/descendants?size=1000";

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
}
