package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.identifiers.NodeIdentifiers;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class NodeDetails extends AbstractNodeElement {
    private final static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public NodeDetails(Node node, OpenBrowser openBrowser) {
        super(null, node, openBrowser);
        fillContent();
    }

    @Override
    protected void fillContent() {
        executor.execute(() -> {
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.add(new NodeAliases(node, openBrowser, node.getAliasesJson()));
            content.add(new NodeIdentifiers("Cross References", node, openBrowser, node.getCrossReferences()));
        });
    }
}
