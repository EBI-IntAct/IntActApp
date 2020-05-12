package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeXRefs;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.Identifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;

import java.util.List;

public class NodeIdentifiers extends AbstractNodeIdentifiers{
    public NodeIdentifiers(IntactNode iNode, OpenBrowser openBrowser) {
        super("Identifiers", iNode, openBrowser);
        fillContent();
    }

    @Override
    protected List<Identifier> getIdentifiersToShow() {
        return iNode.getIdentifiers();
    }
}
