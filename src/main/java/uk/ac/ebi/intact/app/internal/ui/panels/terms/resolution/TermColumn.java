package uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution;

import uk.ac.ebi.intact.app.internal.model.core.Interactor;

import java.util.function.Function;

public enum TermColumn {
    TERM("Terms", true, null, false),
    SELECT("Select",true, null, false),
    PREVIEW("Preview", false, null, false),
    SPECIES("Species", false, (interactor) -> interactor.species, true),
    TYPE("Type", false, (interactor) -> interactor.type, true),
    NAME("Name", false, (interactor) -> interactor.name, false),
    DESCRIPTION("Description", false, (interactor) -> interactor.fullName, false),
    NB_INTERACTIONS("# Interactions", false, (interactor) -> interactor.interactionCount, false),
    ID("ID", false, (interactor) -> interactor.preferredId, false),
    AC("IntAct Ac", false, (interactor) -> interactor.ac, false);

    public final String name;
    public final Boolean isFixedInRowHeader;
    public final Boolean filtered;
    public final Function<Interactor, Object> getValue;

    TermColumn(String name, Boolean isFixedInRowHeader, Function<Interactor, Object> getValue, boolean filtered) {
        this.name = name;
        this.isFixedInRowHeader = isFixedInRowHeader;
        this.getValue = getValue;
        this.filtered = filtered;
    }
}
