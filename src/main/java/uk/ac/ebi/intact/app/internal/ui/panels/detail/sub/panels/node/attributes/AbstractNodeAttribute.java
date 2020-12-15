package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractSelectedElementAttribute;

public abstract class AbstractNodeAttribute extends AbstractSelectedElementAttribute {
    protected final Node node;

    public AbstractNodeAttribute(String title, Node node, OpenBrowser openBrowser) {
        super(title,openBrowser);
        this.node = node;
    }

}
