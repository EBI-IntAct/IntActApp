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

import static uk.ac.ebi.intact.intactApp.internal.io.HttpUtils.getRequestResultForUrl;
import static uk.ac.ebi.intact.intactApp.internal.model.styles.utils.Taxons.*;

public class OLSMapper {
    private static boolean taxIdsReady = false;
    private static boolean taxIdsWorking = false;
    private static boolean nodeTypesReady = false;
    private static boolean nodeTypesWorking = false;
    private static boolean edgeTypesReady = false;
    private static boolean edgeTypesWorking = false;


    public static Hashtable<Long, Paint> taxIdToPaint = new Hashtable<>() {{
        put(E_COLI.taxId, new Color(137, 51, 54)); // Escherichia coli
        put(S_CEREVISIAE.taxId, new Color(74, 147, 121));  // Saccharomyces cerevisiae
        put(H_SAPIENS.taxId, new Color(51, 94, 148));  // Homo sapiens
        put(M_MUSCULUS.taxId, new Color(28, 67, 156)); // Mus musculus
        put(A_THALIANA.taxId, new Color(46, 93, 46));  // Arabidopsis thaliana
        put(D_MELANOGASTER.taxId, new Color(147, 92, 56)); // Drosophila melanogaster
        put(C_ELEGANS.taxId, new Color(174, 125, 52)); // Caenorhabditis elegans
        put(CHEMICAL_SYNTHESIS.taxId, new Color(141, 102, 102));  // Chemical Synthesis
    }};
    public static Hashtable<Long, Paint> kingdomColors = new Hashtable<>() {{
        put(PLANTS.taxId, new Color(80, 162, 79)); // Viridiplantae (Plants)
        put(ANIMALS.taxId, new Color(235, 144, 0)); // Metazoa (Animals)
        put(MAMMALS.taxId, new Color(86, 136, 192)); // Mammals

        put(FUNGI.taxId, new Color(62, 181, 170)); // Fungi
        put(BACTERIA.taxId, new Color(178, 53, 57)); // Bacteria
        put(VIRUSES.taxId, new Color(132, 100, 190)); // Viruses
        put(ARCHAEA.taxId, new Color(101, 101, 101)); // Archaea
    }};

    public static Hashtable<Long, Paint> originalTaxIdToPaint = new Hashtable<>(taxIdToPaint);
    public static Hashtable<Long, Paint> originalKingdomColors = new Hashtable<>(kingdomColors);

    public static Hashtable<Long, List<Long>> taxIdToChildrenTaxIds = new Hashtable<>();

    public static final Hashtable<String, Paint> edgeTypeToPaint = new Hashtable<>() {{
        put("association", new Color(153, 153, 255));
        put("colocalization", new Color(255, 222, 62));
        put("dephosphorylation reaction", new Color(153, 153, 0));
        put("dephosphorylation", new Color(153, 153, 0));
        put("direct interaction", new Color(255, 165, 0));
        put("phosphorylation reaction", new Color(153, 0, 0));
        put("phosphorylation", new Color(153, 0, 0));
        put("physical association", new Color(153, 204, 0));
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
        if (!taxIdsWorking) {
            taxIdsWorking = true;

            for (Long kingdomId : kingdomColors.keySet()) {
                taxIdToChildrenTaxIds.put(kingdomId, new ArrayList<>());
            }

            for (Long parentSpecie : new ArrayList<>(taxIdToPaint.keySet())) {
                Paint paint = taxIdToPaint.get(parentSpecie);
                taxIdToChildrenTaxIds.put(parentSpecie, new ArrayList<>() {{
                    add(parentSpecie);
                }});

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

            taxIdsReady = true;
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
                    if (kingdomColors.containsKey(supTaxId)) {
                        Paint paint = kingdomColors.get(supTaxId);
                        taxIdToPaint.put(taxId, paint);
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

    public static Map<Long, Paint> updateKingdomColor(Long kingdomId, Paint newPaint) {
        Map<Long, Paint> updatedTaxIds = new Hashtable<>();
        for (Long subTaxId : taxIdToChildrenTaxIds.get(kingdomId)) {
            taxIdToPaint.put(subTaxId, newPaint);
            updatedTaxIds.put(subTaxId, newPaint);
        }
        return updatedTaxIds;
    }

    public static void initializeNodeTypeToShape() {
        if (!nodeTypesWorking) {
            nodeTypesWorking = true;
            for (String miType : new ArrayList<>(nodeTypeToShape.keySet())) {
                setChildrenValues(nodeTypeToShape, miType, nodeTypeToShape.get(miType));
            }
            nodeTypesReady = true;
        }
    }

    public static void initializeEdgeTypeToPaint() {
        if (!edgeTypesWorking) {
            edgeTypesWorking = true;

            Map<String, Paint> originalColors = new Hashtable<>(edgeTypeToPaint);

            for (String miType : Arrays.asList("direct interaction", "phosphorylation reaction", "dephosphorylation reaction")) {
                setChildrenValues(edgeTypeToPaint, miType, originalColors.get(miType));
            }
            edgeTypeToPaint.putAll(originalColors);


            edgeTypesReady = true;
        }
    }

    private static <T> void setChildrenValues(Map<String, T> mapToFill, String parentLabel, T parentValue) {
        String jsonQuery = "https://www.ebi.ac.uk/ols/api/ontologies/mi/terms/" +
                "http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252F" + typesToIds.get(parentLabel) + "/descendants?size=1000";

        try {
            boolean hasNext = true;
            while (hasNext) {
                JsonNode json = HttpUtils.getJsonForUrl(jsonQuery);
                if (json != null) {
                    if (json.get("page").get("totalElements").intValue() > 0) {

                        JsonNode termChildren = json.get("_embedded").get("terms");

                        for (final JsonNode objNode : termChildren) {
                            String label = objNode.get("label").textValue().replaceAll(" reaction", "");
                            mapToFill.put(label, parentValue);
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


    public static String searchMIId(String toSearch) {
        String jsonText = getRequestResultForUrl(String.format("https://www.ebi.ac.uk/ols/api/search?q=%s&ontology=mi", toSearch.replaceAll(" ", "%20")));
        if (jsonText.length() > 0) {
            try {
                JsonNode response = new ObjectMapper().readTree(jsonText).get("response");
                if (response.get("numFound").asInt() > 0) {
                    return response.get("docs").get(0).get("obo_id").textValue();
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String concatenateTaxIds(Set<Long> taxIds) {
        return taxIds.toString().replaceAll("[\\[\\]]", "").replaceAll(" ", "%20");
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
