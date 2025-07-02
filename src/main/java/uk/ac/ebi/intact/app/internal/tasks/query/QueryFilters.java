package uk.ac.ebi.intact.app.internal.tasks.query;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class QueryFilters {

    private Set<String> interactorSpeciesFilter;
    private Set<String> interactorTypesFilter;
    private Set<String> interactionTypesFilter;
    private Set<String> interactionDetectionMethodsFilter;
    private Set<String> interactionHostOrganismsFilter;
    private NegativeFilterStatus negativeFilter;
    private Double minMIScore;
    private Double maxMIScore;
    private Boolean mutationFilter;
    private Boolean expansionFilter;

    public enum NegativeFilterStatus {
        POSITIVE_ONLY(false),
        POSITIVE_AND_NEGATIVE(null),
        NEGATIVE_ONLY(true);

        public final Boolean booleanValue;

        NegativeFilterStatus(Boolean booleanValue) {
            this.booleanValue = booleanValue;
        }
    }
}
