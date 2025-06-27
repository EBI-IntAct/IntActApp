package uk.ac.ebi.intact.app.internal.model.filters.edge;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class EdgeHostOrganismFilter extends DiscreteFilter<EvidenceEdge> {

    public EdgeHostOrganismFilter(NetworkView view) {
        super(view,
                EvidenceEdge.class,
                "Host organism",
                "Experimental environment (cell type, tissue, in vitro…) in which the interaction evidence is captured",
                view.getNetwork().getQueryParams() != null ? view.getNetwork().getQueryParams().getInteractionHostOrganismsFilter() : null);
    }

    @Override
    public String getPropertyValue(EvidenceEdge element) {
        return element.hostOrganism;
    }
}
