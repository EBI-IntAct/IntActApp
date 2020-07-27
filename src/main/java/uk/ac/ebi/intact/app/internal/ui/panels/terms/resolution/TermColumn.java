package uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution;

import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;

import java.util.function.Function;

public enum TermColumn {
    TERM("Terms", "", true, null, false),
    SELECT("Select", "", true, null, false),
    PREVIEW("Preview", "", false, null, false),
    SPECIES("Species", "", false, (interactor) -> interactor.species, true),
    TYPE("Type", "", false, (interactor) -> interactor.type, true),
    NAME("Name", "interactor_name", false, (interactor) -> interactor.name, false),
    DESCRIPTION("Description", "interactor_description", false, (interactor) -> interactor.fullName, false),
    NB_INTERACTIONS("# Interactions", "", false, (interactor) -> interactor.interactionCount, false),
    ID("ID", "interactor_identifiers", false, (interactor) -> interactor.preferredId, false),
    MATCHING_COLUMNS("Matching columns", "", false, (interactor) -> interactor.matchingColumns, false),
    AC("IntAct Ac", "interactor_ac", false, (interactor) -> interactor.ac, false);

    public final String name;
    public final String highlightName;
    public final Boolean isFixedInRowHeader;
    public final Function<Interactor, Object> getValue;
    public final Boolean filtered;

    TermColumn(String name, String highlightName, Boolean isFixedInRowHeader, Function<Interactor, Object> getValue, boolean filtered) {
        this.name = name;
        this.highlightName = highlightName;
        this.isFixedInRowHeader = isFixedInRowHeader;
        this.getValue = getValue;
        this.filtered = filtered;
    }

    public static TermColumn getByHighlightName(String highlightName) {
        for (TermColumn termColumn: values()){
            if (termColumn.highlightName.equals(highlightName)) return termColumn;
        }
        return null;
    }
}
