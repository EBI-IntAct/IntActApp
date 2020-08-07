package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractSelectedElementPanel;

abstract class AbstractEdgeElement extends AbstractSelectedElementPanel {
    private final Edge edge;

    public AbstractEdgeElement(String title, Edge edge, OpenBrowser openBrowser) {
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
