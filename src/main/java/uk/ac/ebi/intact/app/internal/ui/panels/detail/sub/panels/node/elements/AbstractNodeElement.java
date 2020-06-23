package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractSelectedElementPanel;

public abstract class AbstractNodeElement extends AbstractSelectedElementPanel {
    protected final Node iNode;

    public AbstractNodeElement(String title, Node iNode, OpenBrowser openBrowser) {
        super(title,openBrowser);
        this.iNode = iNode;
    }

}
