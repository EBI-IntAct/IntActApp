package uk.ac.ebi.intact.intactApp.internal.model.core;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class FeatureClassifier {
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    public static List<FeatureClass> root;
    public static InnerFeatureClass biologic;
    public static InnerFeatureClass experimental;
    public static FeatureClass mutation;
    public static FeatureClass binding;
    public static FeatureClass variant;
    public static FeatureClass tag;

    public static void initMIIdSets() {
        biologic = new InnerFeatureClass("Biological features", "MI:0252", "Post Transcription Modification");
        experimental = new InnerFeatureClass("Experimental features", "MI:0505", "Other experimental features");

        mutation = new FeatureClass("Mutations", "MI:0118");
        binding = new FeatureClass("Binding regions", "MI:0117");
        variant = new FeatureClass("Variants", "MI:1241");
        tag = new FeatureClass("Tags", "MI:0507");

        biologic.subClasses.addAll(List.of(mutation, binding, variant));
        experimental.subClasses.add(tag);
        root = List.of(biologic, experimental);
    }

    public static Map<FeatureClass, List<Feature>> classify(Collection<Feature> features) {
        Map<FeatureClass, List<Feature>> featureClasses = new HashMap<>();
        for (Feature feature : features) {
            FeatureClass featureClass = recursiveFinder(feature.typeMIId, root);
            if (!featureClasses.containsKey(featureClass)) {
                List<Feature> innerFeatures = new ArrayList<>();
                innerFeatures.add(feature);
                featureClasses.put(featureClass, innerFeatures);
            } else {
                featureClasses.get(featureClass).add(feature);
            }
        }
        return featureClasses;
    }

    private static FeatureClass recursiveFinder(String miIdToFind, Collection<FeatureClass> searchInto) {
        for (FeatureClass featureClass : searchInto) {
            if (featureClass.contains(miIdToFind)) {
                if (featureClass instanceof InnerFeatureClass) {
                    InnerFeatureClass innerFeatureClass = (InnerFeatureClass) featureClass;
                    FeatureClass finalClass = recursiveFinder(miIdToFind, innerFeatureClass.subClasses);
                    return finalClass != null ? finalClass : innerFeatureClass.nonDefinedLeaf;
                } else {
                    return featureClass;
                }
            }
        }
        return null;
    }

    public static class FeatureClass {
        public final String name;
        public final String miId;
        public final Set<String> innerMIIds = new HashSet<>();

        public FeatureClass(String name, String miId) {
            this.name = name;
            this.miId = miId;
            executor.execute(() -> {
                innerMIIds.add(miId);
                String jsonQuery = "https://www.ebi.ac.uk/ols/api/ontologies/mi/terms/" +
                        "http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252F" + miId.replaceAll(":", "_") + "/descendants?size=1000";

                try {
                    boolean hasNext = true;
                    while (hasNext) {
                        JsonNode json = HttpUtils.getJsonForUrl(jsonQuery);
                        if (json != null) {
                            if (json.get("page").get("totalElements").intValue() > 0) {
                                JsonNode termChildren = json.get("_embedded").get("terms");
                                for (final JsonNode objNode : termChildren) {
                                    innerMIIds.add(objNode.get("obo_id").textValue());
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
            });
        }

        public FeatureClass(String name) {
            this.name = name;
            miId = null;
        }


        public boolean contains(String miId) {
            return innerMIIds.contains(miId);
        }


        @Override
        public String toString() {
            return name;
        }
    }

    public static class InnerFeatureClass extends FeatureClass {
        public final List<FeatureClass> subClasses = new ArrayList<>();
        public final FeatureClass nonDefinedLeaf;

        public InnerFeatureClass(String name, String miId, String nonDefinedLeafName) {
            super(name, miId);
            this.nonDefinedLeaf = new FeatureClass(nonDefinedLeafName);
        }
    }
}
