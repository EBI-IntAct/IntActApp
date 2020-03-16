package uk.ac.ebi.intact.intactApp.internal.model;

public enum IntactViewType {
    COLLAPSED("collapsed"),
    EXPANDED("expanded"),
    MUTATION("mutation");

    private String name;

    IntactViewType(String name) {
        this.name = name;
    }
}
