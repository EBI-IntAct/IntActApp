package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.AbstractSelectedElementPanel;

public abstract class AbstractNodeElement extends AbstractSelectedElementPanel {
    protected final IntactNode iNode;

    public AbstractNodeElement(String title, IntactNode iNode, OpenBrowser openBrowser) {
        super(title,openBrowser);
        this.iNode = iNode;
    }

}
