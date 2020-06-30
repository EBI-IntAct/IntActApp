package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;

public class EdgeExpansionTypeFilter extends BooleanFilter<EvidenceEdge> {
    public EdgeExpansionTypeFilter(NetworkView view) {
        super(view, EvidenceEdge.class, "Expansion", "Hide spoke expanded");
    }

    @Override
    public boolean isToHide(EvidenceEdge element) {
        return element.expansionType != null && element.expansionType.equals("spoke expansion");
    }
}
