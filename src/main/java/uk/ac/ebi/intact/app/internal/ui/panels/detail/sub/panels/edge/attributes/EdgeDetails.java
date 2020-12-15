package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.attributes;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class EdgeDetails extends AbstractEdgeAttribute {
    private final static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public EdgeDetails(Edge edge, OpenBrowser openBrowser) {
        super(null, edge, openBrowser);
        fillContent();
    }

    @Override
    protected void fillSummaryEdgeContent(SummaryEdge edge) {
        setVisible(false);
    }

    @Override
    protected void fillEvidenceEdgeContent(EvidenceEdge edge) {
        executor.execute(() -> {
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.add(new EdgeAnnotations(edge, openBrowser, edge.getAnnotations()));
            content.add(new EdgeParameters(edge, openBrowser, edge.getParameters()));
        });
    }

}
