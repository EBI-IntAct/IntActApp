package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements;

import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.DbIdentifiersToLink;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.LinkUtils;

import javax.swing.*;
import java.awt.*;

public class NodeBasics extends AbstractNodeElement {

    private LinePanel graphDescription;

    public NodeBasics(IntactNode iNode, OpenBrowser openBrowser) {
        super(null, iNode, openBrowser);
        fillContent();
    }

    @Override
    protected void fillContent() {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(new JLabel(iNode.fullName));
        content.add(new JLink(DbIdentifiersToLink.getFancyDatabaseName(iNode.preferredIdentifier) + " · " + iNode.preferredIdentifier.id, DbIdentifiersToLink.getLink(iNode.preferredIdentifier), openBrowser));
        graphDescription = new LinePanel(getBackground());
        graphDescription.add(new JLabel(StringUtils.capitalize(iNode.type) + " of " + iNode.species));
        graphDescription.add(Box.createHorizontalStrut(4));
        graphDescription.add(LinkUtils.createSpecieLink(openBrowser, iNode.taxId));
        graphDescription.add(Box.createHorizontalGlue());
        content.add(graphDescription);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (graphDescription != null)
            graphDescription.setBackground(bg);
    }
}
