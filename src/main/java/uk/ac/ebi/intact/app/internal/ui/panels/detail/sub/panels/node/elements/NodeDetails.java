package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.Identifier;
import uk.ac.ebi.intact.app.internal.model.core.IntactNode;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.identifiers.NodeIdentifiers;
import uk.ac.ebi.intact.app.internal.model.core.ontology.OntologyIdentifier;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static uk.ac.ebi.intact.app.internal.model.managers.IntactManager.INTACT_GRAPH_WS;

public class NodeDetails extends AbstractNodeElement {
    private final static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public NodeDetails(IntactNode iNode, OpenBrowser openBrowser) {
        super(null, iNode, openBrowser);
        fillContent();
    }

    @Override
    protected void fillContent() {
        executor.execute(() -> {
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            JsonNode nodeDetails = HttpUtils.getJSON(INTACT_GRAPH_WS + "network/node/details/" + iNode.ac, new HashMap<>(), iNode.iNetwork.getManager());
            if (nodeDetails != null) {
                content.add(new NodeAliases(iNode, openBrowser, nodeDetails.get("aliases")));
                addNodeCrossReferences(nodeDetails.get("xrefs"));
            }
        });
    }

    private void addNodeCrossReferences(JsonNode xrefs) {
        if (xrefs == null) return;

        List<Identifier> identifiers = new ArrayList<>();
        for (JsonNode xref : xrefs) {
            JsonNode database = xref.get("database");

            String databaseName = database.get("shortName").textValue();
            OntologyIdentifier databaseIdentifier = new OntologyIdentifier(database.get("identifier").textValue());

            String identifier = xref.get("identifier").textValue();
            String qualifier = xref.get("qualifier").textValue();

            identifiers.add(new Identifier(databaseName, databaseIdentifier, identifier, qualifier));
        }
        content.add(new NodeIdentifiers("Cross References", iNode, openBrowser, identifiers));
    }
}
