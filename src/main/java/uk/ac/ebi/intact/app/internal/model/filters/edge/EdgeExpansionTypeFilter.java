package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.BooleanFilter;

public class EdgeExpansionTypeFilter extends BooleanFilter<EvidenceEdge> {
    public EdgeExpansionTypeFilter(NetworkView view) {
        super(view, EvidenceEdge.class, "Expansion", "The method by which complex n-ary data is expanded into binary data.", "Hide spoke expanded");
    }

    @Override
    public boolean isToHide(EvidenceEdge element) {
        return element.expansionType != null && element.expansionType.equals("spoke expansion");
    }
}
