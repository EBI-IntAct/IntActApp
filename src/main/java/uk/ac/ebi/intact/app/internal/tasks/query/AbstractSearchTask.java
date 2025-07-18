package uk.ac.ebi.intact.app.internal.tasks.query;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Tunable;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;

import java.util.Set;

public abstract class AbstractSearchTask extends AbstractTask {

    @Tunable(context = "nogui", exampleStringValue = "Mus musculus,Homo sapiens",
            description = "Filters interactions based on interactor species (comma separated)")
    public String interactorSpeciesFilter;

    @Tunable(context = "nogui", exampleStringValue = "In vitro,Homo sapiens",
            description = "Filters interactions based on interaction host organism (comma separated)")
    public String interactionHostOrganismsFilter;

    @Tunable(context = "nogui", exampleStringValue = "MI:0326,MI:0327",
            description = "Filters interactions based on interactor types (MI ids, comma separated)")
    public String interactorTypesFilter;

    @Tunable(context = "nogui", exampleStringValue = "MI:0915,MI:2364",
            description = "Filters interactions based on interaction types (MI ids, comma separated)")
    public String interactionTypesFilter;

    @Tunable(context = "nogui", exampleStringValue = "MI:0007,MI:0397",
            description = "Filters interactions based on interaction detection method (MI ids, comma separated)")
    public String interactionDetectionMethodsFilter;

    @Tunable(context = "nogui", exampleStringValue = "MI:0007,MI:0081",
            description = "Filters interactions based on participant detection method (MI ids, comma separated)")
    public String participantDetectionMethodsFilter;

    @Tunable(context = "nogui", exampleStringValue = "false",
            description = "Filters to get only negative interactions")
    public String negativeFilter;

    @Tunable(context = "nogui", exampleStringValue = "0",
            description = "Filters to get interaction with MI-score above value")
    public Double minMIScore;

    @Tunable(context = "nogui", exampleStringValue = "1",
            description = "Filters to get interaction with MI-score under value")
    public Double maxMIScore;

    @Tunable(context = "nogui", exampleStringValue = "false",
            description = "Filters to get only interactions affected by mutation")
    public Boolean mutationFilter;

    @Tunable(context = "nogui", exampleStringValue = "false",
            description = "Filters to get only non expanded interactions")
    public Boolean expansionFilter;

    @Tunable(context = "nogui", exampleStringValue = "false",
            description = "Parameter to display network on mutation view")
    public Boolean mutationStyle;

    @Tunable(context = "nogui", exampleStringValue = "false",
            description = "Parameter to display network on evidence view")
    public Boolean expanded;

    protected QueryFilters getQueryParams() {
        return QueryFilters.builder()
                .interactorSpeciesFilter(paramToSet(interactorSpeciesFilter))
                .interactorTypesFilter(paramToSet(interactorTypesFilter))
                .interactionTypesFilter(paramToSet(interactionTypesFilter))
                .interactionDetectionMethodsFilter(paramToSet(interactionDetectionMethodsFilter))
                .participantDetectionMethodsFilter(paramToSet(participantDetectionMethodsFilter))
                .interactionHostOrganismsFilter(paramToSet(interactionHostOrganismsFilter))
                .negativeFilter(negativeFilter != null
                        ? QueryFilters.NegativeFilterStatus.valueOf(negativeFilter)
                        : null)
                .minMIScore(minMIScore)
                .maxMIScore(maxMIScore)
                .mutationFilter(mutationFilter)
                .expansionFilter(expansionFilter)
                .build();
    }

    protected NetworkView.Type getNetworkViewType() {
        if (mutationStyle != null && mutationStyle) {
            return NetworkView.Type.MUTATION;
        } else if (expanded != null && expanded) {
            return NetworkView.Type.EVIDENCE;
        }
        return NetworkView.Type.SUMMARY;
    }

    private Set<String> paramToSet(String param) {
        if (param != null) {
            return Set.of(param.split(","));
        }
        return null;
    }
}
