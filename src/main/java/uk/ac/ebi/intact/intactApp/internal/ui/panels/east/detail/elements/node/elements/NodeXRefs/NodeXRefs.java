package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeXRefs;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.core.Identifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class NodeXRefs extends AbstractNodeIdentifiers{
    private final static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

    public NodeXRefs(IntactNode iNode, OpenBrowser openBrowser) {
        super("Cross references", iNode, openBrowser);
        executor.execute(this::fillContent);
    }

    @Override
    protected List<Identifier> getIdentifiersToShow() {
        List<Identifier> identifiers = new ArrayList<>();
        JsonNode root = HttpUtils.getJsonForUrl("https://wwwdev.ebi.ac.uk/intact/ws/graph/network/node/details/" + iNode.id);
        if (root != null) {
            for (JsonNode xref: root.get("xrefs")) {
                JsonNode database = xref.get("database");
                identifiers.add(new Identifier(iNode, database.get("shortName").textValue(), database.get("identifier").textValue(), xref.get("identifier").textValue()));

            }
        }
        return identifiers;
    }

}
