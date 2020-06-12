package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.identifiers.panels;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.DbIdentifiersToLink;
import uk.ac.ebi.intact.intactApp.internal.model.core.Identifier;
import uk.ac.ebi.intact.intactApp.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.CollapsablePanel;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;

public class IdentifierPanel extends CollapsablePanel {
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);
    protected final List<Identifier> identifiers;
    protected final OpenBrowser openBrowser;
    protected IdentifierPanel(List<Identifier> identifiers, OpenBrowser openBrowser) {
        super(DbIdentifiersToLink.getFancyDatabaseName(identifiers.get(0)) + " (" + identifiers.size() + ")", true);
        this.identifiers = identifiers;
        this.openBrowser = openBrowser;
        setBackground(backgroundColor);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        executor.execute(this::fillContent);
    }

    protected void fillContent() {
        for (Identifier identifier : identifiers) {
            addContent(new JLink(identifier.id, DbIdentifiersToLink.getLink(identifier), openBrowser));
        }
    }
}
