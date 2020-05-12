package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements;

import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.LinkUtils;

import javax.swing.*;
import java.awt.*;

public class NodeBasics extends AbstractNodeElement {
    public NodeBasics(IntactNode iNode, OpenBrowser openBrowser) {
        super(null, iNode, openBrowser);
        fillContent();
    }

    @Override
    protected void fillContent() {
        content.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));
        content.add(new JLabel(StringUtils.capitalize(iNode.type) + " of " + iNode.species));
        content.add(Box.createHorizontalStrut(4));
        content.add(LinkUtils.createSpecieLink(iNode.taxId, openBrowser));
        content.add(Box.createHorizontalGlue());
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
    }
}
