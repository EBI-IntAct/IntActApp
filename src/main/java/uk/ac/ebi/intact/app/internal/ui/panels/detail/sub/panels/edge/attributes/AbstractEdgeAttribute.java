package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.attributes;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractSelectedElementAttribute;

abstract class AbstractEdgeAttribute extends AbstractSelectedElementAttribute {
    private final Edge edge;

    public AbstractEdgeAttribute(String title, Edge edge, OpenBrowser openBrowser) {
        super(title, openBrowser);
        this.edge = edge;
    }

    @Override
    protected void fillContent() {
        if (edge instanceof SummaryEdge) {
            fillSummaryEdgeContent((SummaryEdge) edge);
        } else if (edge instanceof EvidenceEdge) {
            fillEvidenceEdgeContent((EvidenceEdge) edge);
        }
    }

    protected abstract void fillSummaryEdgeContent(SummaryEdge edge);

    protected abstract void fillEvidenceEdgeContent(EvidenceEdge edge);
}
