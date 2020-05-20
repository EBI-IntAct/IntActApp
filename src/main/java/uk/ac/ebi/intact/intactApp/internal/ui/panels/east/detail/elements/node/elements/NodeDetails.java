package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.core.Identifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.identifiers.NodeIdentifiers;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
            Map<Object, Object> postData = new HashMap<>();
            postData.put("ids", List.of(iNode.id));
            JsonNode nodeDetails = HttpUtils.postJSON("https://wwwdev.ebi.ac.uk/intact/ws/graph/network/node/details", postData, iNode.iNetwork.getManager());
            if (nodeDetails != null) {
                JsonNode onlyNodeDetails = nodeDetails.get(0);
                content.add(new NodeAliases(iNode, openBrowser, onlyNodeDetails.get("aliases")));
                addNodeCrossReferences(onlyNodeDetails.get("xrefs"));
            }
        });
    }

    private void addNodeCrossReferences(JsonNode xrefs) {
        List<Identifier> identifiers = new ArrayList<>();
        for (JsonNode xref : xrefs) {
            JsonNode database = xref.get("database");

            String databaseName = database.get("shortName").textValue();
            OntologyIdentifier databaseIdentifier = new OntologyIdentifier(database.get("identifier").textValue());

            String identifier = xref.get("identifier").textValue();
            String qualifier = xref.get("qualifier").textValue();

            identifiers.add(new Identifier(iNode, databaseName, databaseIdentifier, identifier, qualifier));
        }
        content.add(new NodeIdentifiers("Cross References", iNode, openBrowser, identifiers));
    }
}
