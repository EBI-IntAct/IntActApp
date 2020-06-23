package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.elements;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.app.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;

import javax.swing.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class EdgeDetails extends AbstractEdgeElement {
    private final static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public EdgeDetails(IntactEdge iEdge, OpenBrowser openBrowser) {
        super(null, iEdge, openBrowser);
        fillContent();
    }

    @Override
    protected void fillCollapsedEdgeContent(IntactCollapsedEdge edge) {
        setVisible(false);
    }

    @Override
    protected void fillEvidenceEdgeContent(IntactEvidenceEdge edge) {
        executor.execute(() -> {
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            JsonNode edgeDetails = HttpUtils.getJSON(IntactManager.INTACT_GRAPH_WS + "network/edge/details/" + edge.id, new HashMap<>(), edge.iNetwork.getManager());
            if (edgeDetails != null) {
                content.add(new EdgeAnnotations(edge, openBrowser, edgeDetails.get("annotations")));
                content.add(new EdgeParameters(edge, openBrowser, edgeDetails.get("parameters")));
            }
        });
    }

}
