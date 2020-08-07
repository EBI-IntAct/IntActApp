package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractSelectedElementPanel;

public abstract class AbstractNodeElement extends AbstractSelectedElementPanel {
    protected final Node node;

    public AbstractNodeElement(String title, Node node, OpenBrowser openBrowser) {
        super(title,openBrowser);
        this.node = node;
    }

}
