package uk.ac.ebi.intact.intactApp.internal.model.styles.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created by anjali on 13/06/19.
 */
public class OLSMapper {
    private static boolean taxIdsReady = false,
            taxIdsWorking = false,
            nodeTypesReady = false,
            nodeTypesWorking = false,
            edgeTypesReady = false,
            edgeTypesWorking = false;


    public static final Hashtable<Long, Paint> taxIdToPaint = new Hashtable<>() {{
        put(562L, new Color(144, 163, 198));
        put(4932L, new Color(107, 13, 10));
        put(9606L, new Color(51, 94, 148));
        put(10090L, new Color(88, 115, 29));
        put(3702L, new Color(97, 74, 124));
        put(7227L, new Color(47, 132, 156));
        put(6239L, new Color(202, 115, 47));
        put(-2L, new Color(141, 102, 102));
    }};

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

            for (Long parentSpecie : new ArrayList<>(taxIdToPaint.keySet())) {
                Paint paint = taxIdToPaint.get(parentSpecie);

                String jsonQuery = "https://www.ebi.ac.uk/ols/api/ontologies/ncbitaxon/terms/" +
                        "http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCBITaxon_" + parentSpecie + "/descendants?size=1000";
                try {
                    boolean hasNext = true;
                    while (hasNext) {
                        String jsonText = getJsonForUrl(jsonQuery);// mainQry
                        if (jsonText.length() > 0) {
                            JsonNode json = new ObjectMapper().readTree(jsonText);
                            if (json.get("page").get("totalElements").intValue() > 0) {
                                JsonNode termChildren = json.get("_embedded").get("terms");

                                for (final JsonNode objNode : termChildren) {
                                    String obo_id = objNode.get("obo_id").textValue();
                                    Long id = Long.parseLong(obo_id.substring(obo_id.indexOf(":") + 1));
                                    taxIdToPaint.put(id, paint);

                                }
                            }

                            JsonNode nextPage = json.get("_links").get("next");
                            if (nextPage != null) {
                                jsonQuery = nextPage.get("href").textValue();
                            } else {
                                hasNext = false;
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            taxIdsReady = true;
        }
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
                String jsonText = getJsonForUrl(jsonQuery);// mainQry
                if (jsonText.length() > 0) {
                    JsonNode json = new ObjectMapper().readTree(jsonText);
                    System.out.println(jsonText);
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
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getJsonForUrl(String jsonQuery) {
        String jsonText = "";
        try {
            URL url = new URL(jsonQuery);
            URLConnection olsConnection = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(olsConnection.getInputStream()));
            String inputLine;
            StringBuilder builder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
            jsonText = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonText;
    }

    public static String searchMIId(String toSearch) {
        String jsonText = getJsonForUrl(String.format("https://www.ebi.ac.uk/ols/api/search?q=%s&ontology=mi", toSearch.replaceAll(" ", "%20")));
        if (jsonText.length() > 0) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(jsonText);
                JsonNode response = jsonNode.get("response");
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
