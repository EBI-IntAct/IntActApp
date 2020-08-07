package uk.ac.ebi.intact.app.internal.model.core.elements.nodes;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import java.util.*;

public class Interactor {
    public final String ac;
    public final String name;
    public final String preferredId;
    public final String description;
    public final String typeName;
    public final String species;
    public final Map<String, List<String>> matchingColumns = new HashMap<>();
    public final String taxId;
    public final Integer interactionCount;


    public Interactor(String ac, String name, String preferredId, String description, String typeName, String species, String taxId, Integer interactionCount) {
        this.ac = ac;
        this.name = name;
        this.preferredId = preferredId;
        this.description = description;
        this.typeName = typeName;
        this.species = species;
        this.taxId = taxId;
        this.interactionCount = interactionCount;
    }

    public static Map<String, List<Interactor>> getInteractorsToResolve(JsonNode json, Map<String, Integer> totalInteractors) {
        Map<String, List<Interactor>> map = new HashMap<>();
        return getInteractorsToResolve(json, map, totalInteractors);
    }

    public static Map<String, List<Interactor>> getInteractorsToResolve(JsonNode json, Map<String, List<Interactor>> map, Map<String, Integer> totalInteractors) {
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
                        entity.get("interactorTaxId").asText(),
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
            totalInteractors.put(termName, termData.get("totalElements").intValue());
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
                ", description='" + description + '\'' +
                ", type='" + typeName + '\'' +
                ", species='" + species + '\'' +
                ", taxId=" + taxId +
                ", interactionCount=" + interactionCount +
                '}';
    }
}
