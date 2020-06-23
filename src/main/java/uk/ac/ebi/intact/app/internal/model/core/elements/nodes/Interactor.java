package uk.ac.ebi.intact.app.internal.model.core.elements.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import java.util.*;

public class Interactor {
    public final String ac;
    public final String name;
    public final String preferredId;
    public final String fullName;
    public final String type;
    public final String species;
    public final Map<String, List<String>> matchingColumns = new HashMap<>();
    public final Long taxId;
    public final Integer interactionCount;


    public Interactor(String ac, String name, String preferredId, String fullName, String type, String species, Long taxId, Integer interactionCount) {
        this.ac = ac;
        this.name = name;
        this.preferredId = preferredId;
        this.fullName = fullName;
        this.type = type;
        this.species = species;
        this.taxId = taxId;
        this.interactionCount = interactionCount;
    }

    public static Map<String, List<Interactor>> getInteractorsToResolve(JsonNode json, Map<String, Boolean> pagedTerms) {
        Map<String, List<Interactor>> map = new HashMap<>();
        return getInteractorsToResolve(json, map, pagedTerms);
    }

    public static Map<String, List<Interactor>> getInteractorsToResolve(JsonNode json, Map<String, List<Interactor>> map, Map<String, Boolean> pagedTerms) {
        json.fields().forEachRemaining(term -> {
            List<Interactor> interactors = new ArrayList<>();
            JsonNode termData = term.getValue();
            for (JsonNode highlighted : termData.get("highlighted")) {
                JsonNode entity = highlighted.get("entity");
                Interactor interactor = new Interactor(
                        entity.get("interactorAc").textValue(),
                        entity.get("interactorName").textValue(),
                        entity.get("interactorPreferredIdentifier").textValue(),
                        entity.get("interactorDescription").textValue(),
                        entity.get("interactorType").textValue(),
                        entity.get("interactorSpecies").textValue(),
                        entity.get("interactorTaxId").longValue(),
                        entity.get("interactionCount").intValue()
                );
                for (JsonNode highlight : highlighted.get("highlights")) {
                    for (JsonNode snipplet : highlight.get("snipplets")) {
                        CollectionUtils.addToGroups(interactor.matchingColumns, snipplet.textValue(), s -> highlight.get("field").get("name").textValue());
                    }
                }
                interactors.add(interactor);
            }
            String termName = term.getKey();
            map.put(termName, interactors);
            pagedTerms.put(termName, !termData.get("last").booleanValue());
        });

        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interactor that = (Interactor) o;
        return ac.equals(that.ac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ac);
    }

    @Override
    public String toString() {
        return "Interactor{" +
                "ac='" + ac + '\'' +
                ", name='" + name + '\'' +
                ", preferredId='" + preferredId + '\'' +
                ", description='" + fullName + '\'' +
                ", type='" + type + '\'' +
                ", species='" + species + '\'' +
                ", taxId=" + taxId +
                ", interactionCount=" + interactionCount +
                '}';
    }
}
