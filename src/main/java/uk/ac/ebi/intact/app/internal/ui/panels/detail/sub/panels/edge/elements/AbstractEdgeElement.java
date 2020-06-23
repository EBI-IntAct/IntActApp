package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractSelectedElementPanel;

abstract class AbstractEdgeElement extends AbstractSelectedElementPanel {
    private final IntactEdge iEdge;

    public AbstractEdgeElement(String title, IntactEdge iEdge, OpenBrowser openBrowser) {
        super(title, openBrowser);
        this.iEdge = iEdge;
    }

    @Override
    protected void fillContent() {
        if (iEdge instanceof IntactCollapsedEdge) {
            fillCollapsedEdgeContent((IntactCollapsedEdge) iEdge);
        } else if (iEdge instanceof IntactEvidenceEdge) {
            fillEvidenceEdgeContent((IntactEvidenceEdge) iEdge);
        }
    }

    protected abstract void fillCollapsedEdgeContent(IntactCollapsedEdge edge);

    protected abstract void fillEvidenceEdgeContent(IntactEvidenceEdge edge);
}
