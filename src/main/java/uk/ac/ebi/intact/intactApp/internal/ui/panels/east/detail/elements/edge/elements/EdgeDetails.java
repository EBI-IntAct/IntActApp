package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.edge.elements;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;

import javax.swing.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static uk.ac.ebi.intact.intactApp.internal.model.IntactManager.INTACT_ENDPOINT_URL;

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
            JsonNode edgeDetails = HttpUtils.getJSON(INTACT_ENDPOINT_URL + "/network/edge/details/" + edge.id, new HashMap<>(), edge.iNetwork.getManager());
            if (edgeDetails != null) {
                content.add(new EdgeAnnotations(edge, openBrowser, edgeDetails.get("annotations")));
                content.add(new EdgeParameters(edge, openBrowser, edgeDetails.get("parameters")));
            }
        });
    }

}
