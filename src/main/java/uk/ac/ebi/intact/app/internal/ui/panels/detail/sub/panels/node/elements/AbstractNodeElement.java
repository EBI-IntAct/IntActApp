package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.IntactNode;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractSelectedElementPanel;

public abstract class AbstractNodeElement extends AbstractSelectedElementPanel {
    protected final IntactNode iNode;

    public AbstractNodeElement(String title, IntactNode iNode, OpenBrowser openBrowser) {
        super(title,openBrowser);
        this.iNode = iNode;
    }

}
