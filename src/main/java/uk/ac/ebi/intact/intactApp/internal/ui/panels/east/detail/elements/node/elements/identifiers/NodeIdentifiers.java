package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.identifiers;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.Identifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.AbstractNodeElement;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.identifiers.panels.IdentifierPanelFactory;
import uk.ac.ebi.intact.intactApp.internal.utils.CollectionUtils;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class NodeIdentifiers extends AbstractNodeElement {

    private final List<Identifier> identifiers;

    public NodeIdentifiers(IntactNode iNode, OpenBrowser openBrowser) {
        this("Identifiers", iNode, openBrowser, iNode.getIdentifiers());
    }

    public NodeIdentifiers(String title, IntactNode iNode, OpenBrowser openBrowser, List<Identifier> identifiers) {
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
        content.setAlignmentX(LEFT_ALIGNMENT);

        for (List<Identifier> identifiersOfDB : dbToIdentifiers.values()) {
            content.add(IdentifierPanelFactory.createPanel(identifiersOfDB, openBrowser));
        }
    }
}
