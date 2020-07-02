package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements;

import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.io.DbIdentifiersToLink;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.utils.LinkUtils;

import javax.swing.*;
import java.awt.*;

public class NodeBasics extends AbstractNodeElement {

    private LinePanel graphDescription;

    public NodeBasics(Node node, OpenBrowser openBrowser) {
        super(null, node, openBrowser);
        fillContent();
    }

    @Override
    protected void fillContent() {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(new JLabel(node.fullName));
        content.add(new JLink(DbIdentifiersToLink.getFancyDatabaseName(node.preferredIdentifier) + " · " + node.preferredIdentifier.id, DbIdentifiersToLink.getLink(node.preferredIdentifier), openBrowser));
        graphDescription = new LinePanel(getBackground());
        graphDescription.add(new JLink(StringUtils.capitalize(node.type), node.typeMIId.getUserAccessURL(), openBrowser));
        graphDescription.add(new JLabel(" of " + node.species));
        graphDescription.add(Box.createHorizontalStrut(4));
        graphDescription.add(LinkUtils.createSpecieLink(openBrowser, node.taxId));
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
