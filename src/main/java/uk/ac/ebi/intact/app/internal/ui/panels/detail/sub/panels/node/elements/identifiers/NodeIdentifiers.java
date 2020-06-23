package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.identifiers;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.Identifier;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.AbstractNodeElement;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.identifiers.panels.IdentifierPanelFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class NodeIdentifiers extends AbstractNodeElement {

    private final List<Identifier> identifiers;

    public NodeIdentifiers(Node iNode, OpenBrowser openBrowser) {
        this("Identifiers", iNode, openBrowser, iNode.getIdentifiers());
    }

    public NodeIdentifiers(String title, Node iNode, OpenBrowser openBrowser, List<Identifier> identifiers) {
        super(title, iNode, openBrowser);
        this.identifiers = identifiers;
        fillContent();
    }

    protected void fillContent() {
        if (identifiers.isEmpty()) {
            this.setVisible(false);
            return;
        }

        Map<String, List<Identifier>> dbToIdentifiers = CollectionUtils.groupBy(identifiers, identifier -> identifier.databaseName);

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (List<Identifier> identifiersOfDB : dbToIdentifiers.values()) {
            content.add(IdentifierPanelFactory.createPanel(identifiersOfDB, openBrowser));
        }
    }
}
