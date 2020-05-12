package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeXRefs;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.DbIdentifiersToLink;
import uk.ac.ebi.intact.intactApp.internal.model.core.Identifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.ui.components.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.JLink;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.AbstractNodeElement;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;
import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.textFont;

public abstract class AbstractNodeIdentifiers extends AbstractNodeElement {

    public AbstractNodeIdentifiers(String title, IntactNode iNode, OpenBrowser openBrowser) {
        super(title, iNode, openBrowser);
    }

    protected void fillContent() {
        Map<String, List<Identifier>> dbToIdentifiers = new HashMap<>();

        List<Identifier> identifiersToShow = getIdentifiersToShow();
        if (identifiersToShow.isEmpty()) {
            this.setVisible(false);
            return;
        }

        for (Identifier identifier : identifiersToShow) {
            if (!dbToIdentifiers.containsKey(identifier.databaseName)) {
                List<Identifier> refs = new ArrayList<>();
                refs.add(identifier);
                dbToIdentifiers.put(identifier.databaseName, refs);
            } else {
                dbToIdentifiers.get(identifier.databaseName).add(identifier);
            }
        }

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentX(LEFT_ALIGNMENT);

        for (List<Identifier> identifiersOfDB : dbToIdentifiers.values()) {
            JPanel dbIdentifiers = new JPanel();
            dbIdentifiers.setBackground(backgroundColor);
            dbIdentifiers.setLayout(new BoxLayout(dbIdentifiers, BoxLayout.Y_AXIS));

            for (Identifier id : identifiersOfDB) {
                JLink link = new JLink(id.id, DbIdentifiersToLink.getLink(id), openBrowser);
                link.setFont(textFont);
                dbIdentifiers.add(link);
            }
            content.add(new CollapsablePanel(DbIdentifiersToLink.getFancyDatabaseName(identifiersOfDB.get(0)) + " (" + identifiersOfDB.size() + ")", dbIdentifiers, true));
        }
    }

    protected abstract List<Identifier> getIdentifiersToShow();
}
