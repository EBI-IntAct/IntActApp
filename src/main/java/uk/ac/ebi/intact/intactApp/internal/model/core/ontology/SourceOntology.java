package uk.ac.ebi.intact.intactApp.internal.model.core.ontology;

import java.util.function.Function;

public enum SourceOntology {
    MI("Molecular Interaction","https://www.ebi.ac.uk/ols/ontologies/mi/terms?iri=http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2F{id}&viewMode=All&siblings=false",
            "https://www.ebi.ac.uk/ols/api/ontologies/mi/terms?iri=http://purl.obolibrary.org/obo/{id}",
            "https://www.ebi.ac.uk/ols/api/ontologies/mi/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252F{id}/descendants?size=1000",
            s -> s.replaceAll(":", "_")),
    MOD("Protein Modification","https://www.ebi.ac.uk/ols/ontologies/mod/terms?iri=http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2F{id}&viewMode=All&siblings=false",
            "https://www.ebi.ac.uk/ols/api/ontologies/mod/terms?iri=http://purl.obolibrary.org/obo/{id}",
            "https://www.ebi.ac.uk/ols/api/ontologies/mod/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252F{id}/descendants?size=1000",
            s -> s.replaceAll(":", "_")),
    PAR("PAR","", "", "", s -> s);

    public final String name;
    final String userAccessURL;
    final String detailsURL;
    final String descendantsURL;
    final Function<String, String> idToURLId;

    SourceOntology(String name, String userAccessURL, String detailsURL, String descendantsURL, Function<String, String> idToURLId) {
        this.name = name;
        this.userAccessURL = userAccessURL;
        this.detailsURL = detailsURL;
        this.descendantsURL = descendantsURL;
        this.idToURLId = idToURLId;
    }

    public String getUserAccessURL(String id) {
        return userAccessURL.replaceAll("\\{id}", idToURLId.apply(id));
    }

    public String getDetailsURL(String id) {
        return detailsURL.replaceAll("\\{id}", idToURLId.apply(id));
    }

    public String getDescendantsURL(String id) {
        return descendantsURL.replaceAll("\\{id}", idToURLId.apply(id));
    }

    @Override
    public String toString() {
        return name;
    }
}
