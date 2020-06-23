package uk.ac.ebi.intact.app.internal.model.core.elements.nodes;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public class Interactor {
    public final String ac;
    public final String name;
    public final String preferredId;
    public final String fullName;
    public final String type;
    public final String species;
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

    public static Map<String, List<Interactor>> getInteractorsToResolve(JsonNode json) {
        Map<String, List<Interactor>> map = new HashMap<>();
        return getInteractorsToResolve(json, map);
    }

    public static Map<String, List<Interactor>> getInteractorsToResolve(JsonNode json, Map<String, List<Interactor>> map) {
        json.fields().forEachRemaining(term -> {
            List<Interactor> interactors = new ArrayList<>();
            for (JsonNode content: term.getValue().get("content")) {
                interactors.add(new Interactor(
                        content.get("interactorAc").textValue(),
                        content.get("interactorName").textValue(),
                        content.get("interactorPreferredIdentifier").textValue(),
                        content.get("interactorDescription").textValue(),
                        content.get("interactorType").textValue(),
                        content.get("interactorSpecies").textValue(),
                        content.get("interactorTaxId").longValue(),
                        content.get("interactionCount").intValue()
                ));
            }
            map.put(term.getKey(), interactors);
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
