package uk.ac.ebi.intact.intactApp.internal.model;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.*;

public class Annotation {
    String annotation;
    int taxId;
    String stringId;
    String query;
    String preferredName;

    public Annotation(String preferredName, String stringId, int taxId, String query, String annotation) {
        this.preferredName = preferredName;
        this.stringId = stringId;
        this.taxId = taxId;
        this.query = query;
        this.annotation = annotation;
    }

    public static Map<String, List<Annotation>> getAnnotations(JsonNode json, String queryTerms) {
        Map<String, List<Annotation>> map = new HashMap<>();
        return getAnnotations(json, queryTerms, map);
    }

    public static Map<String, List<Annotation>> getAnnotations(JsonNode json, String queryTerms,
                                                               Map<String, List<Annotation>> map) {
        String[] terms = queryTerms.trim().split("\n");
        JsonNode annotationArray = ModelUtils.getResultsFromJSON(json);
        Integer version = ModelUtils.getVersionFromJSON(json);

        // If we switch the API back to use a start of 0, this will need to change
        int queryIndexStart = 0;
        if (version != null && version == 1)
            queryIndexStart = -1;

        for (JsonNode ann : annotationArray) {
            String annotation = null;
            String stringId = null;
            String preferredName = null;
            int taxId = -1;
            int queryIndex = -1;

            if (ann.has("preferredName"))
                preferredName =  ann.get("preferredName").textValue();
            if (ann.has("annotation"))
                annotation =  ann.get("annotation").textValue();
            if (ann.has("stringId"))
                stringId =  ann.get("stringId").textValue();
            if (ann.has("ncbiTaxonId"))
                taxId = ann.get("ncbiTaxonId").intValue();
            if (ann.has("queryIndex")) {
                queryIndex = ann.get("queryIndex").intValue() - queryIndexStart;
            }

            Annotation newAnnotation = new Annotation(preferredName, stringId, taxId, terms[queryIndex], annotation);
            if (!map.containsKey(terms[queryIndex])) {
                map.put(terms[queryIndex], new ArrayList<>());
            }

            // Now, look for direct matches
            List<Annotation> annList = map.get(terms[queryIndex]);
            if (annList.size() > 0 && annList.get(0).getPreferredName().equalsIgnoreCase(terms[queryIndex])) {
                continue;
            } else if (preferredName.equalsIgnoreCase(terms[queryIndex])) {
                map.put(terms[queryIndex], Collections.singletonList(newAnnotation));
            } else {
                map.get(terms[queryIndex]).add(newAnnotation);
            }

        }
        return map;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public int getTaxId() {
        return taxId;
    }

    public String getQueryString() {
        return query;
    }

    public String getStringId() {
        return stringId;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String toString() {
        String res = "   Query: " + query + "\n";
        res += "   PreferredName: " + preferredName + "\n";
        res += "   Annotation: " + annotation;
        return res;
    }
}
