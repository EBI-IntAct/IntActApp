package uk.ac.ebi.intact.app.internal.model.core.features;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class FeatureClassifier {
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    public static List<FeatureClass> root;
    public static InnerFeatureClass biological;
    public static InnerFeatureClass experimental;
    public static InnerFeatureClass mutation;
    public static FeatureClass binding;
    public static FeatureClass variant;
    public static FeatureClass tag;

    public static void initMIIdSets() {
        biological = new InnerFeatureClass("Biological features", "MI:0252", "Post Transcription Modification");
        experimental = new InnerFeatureClass("Experimental features", "MI:0505", "Other experimental features");

        mutation = new InnerFeatureClass("Mutations", "MI:0118", "Mutation with undefined effect");

        binding = new FeatureClass("Binding regions", "MI:0117");
        variant = new FeatureClass("Variants", "MI:1241");
        tag = new FeatureClass("Tags", "MI:0507");

        mutation.subClasses.add(new FeatureClass("Mutation causing an interaction", "MI:2227"));
        mutation.subClasses.add(new FeatureClass("Mutation decreasing interaction", "MI:0119"));
        mutation.subClasses.add(new FeatureClass("Mutation increasing interaction", "MI:0382"));
        mutation.subClasses.add(new FeatureClass("Mutation with complex effect", "MI:2333"));
        mutation.subClasses.add(new FeatureClass("Mutation with no effect", "MI:2226"));
        biological.subClasses.addAll(List.of(binding, mutation, variant));
        experimental.subClasses.add(tag);
        root = List.of(biological, experimental);
    }

    public static Map<FeatureClass, List<Feature>> classify(Collection<Feature> features) {
        return CollectionUtils.groupBy(features, feature -> recursiveFinder(feature, root));
    }

    private static FeatureClass recursiveFinder(Feature feature, Collection<FeatureClass> searchInto) {
        for (FeatureClass featureClass : searchInto) {
            if (featureClass.contains(feature.type.id)) {
                if (featureClass instanceof InnerFeatureClass) {
                    InnerFeatureClass innerFeatureClass = (InnerFeatureClass) featureClass;
                    FeatureClass finalClass = recursiveFinder(feature, innerFeatureClass.subClasses);
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
        public final OntologyIdentifier identifier;
        public final Set<OntologyIdentifier> innerIdentifiers = new HashSet<>();

        public FeatureClass(String name, String identifier) {
            this.name = name;
            this.identifier = new OntologyIdentifier(identifier);
            executor.execute(() -> {
                innerIdentifiers.add(this.identifier);

                String jsonQuery = this.identifier.getDescendantsURL();

                try {
                    boolean hasNext = true;
                    while (hasNext) {
                        JsonNode json = HttpUtils.getJsonForUrl(jsonQuery);
                        if (json != null) {
                            if (json.get("page").get("totalElements").intValue() > 0) {
                                JsonNode termChildren = json.get("_embedded").get("terms");
                                for (final JsonNode objNode : termChildren) {
                                    innerIdentifiers.add(new OntologyIdentifier(objNode.get("obo_id").textValue(), this.identifier.sourceOntology));
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
            identifier = null;
        }


        public boolean contains(OntologyIdentifier id) {
            return innerIdentifiers.contains(id);
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
