package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.identifiers.panels;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.io.DbIdentifiersToLink;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.Identifier;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.app.internal.ui.components.panels.GroupingCVPanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static uk.ac.ebi.intact.app.internal.model.styles.UIColors.lightBackground;

public class IdentifierPanel extends GroupingCVPanel<Identifier> {
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);
    protected final List<Identifier> identifiers;
    protected final OpenBrowser openBrowser;

    protected IdentifierPanel(List<Identifier> identifiers, OpenBrowser openBrowser) {
        super(new VerticalPanel(), identifiers.get(0).database, DbIdentifiersToLink::getFancyDatabaseName, identifiers, lightBackground, true, openBrowser);
        this.identifiers = identifiers;
        this.openBrowser = openBrowser;
        setBackground(lightBackground);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        executor.execute(this::fillContent);
    }

    protected void fillContent() {
        for (Identifier identifier : identifiers) {
            addContent(new JLink(identifier.id, DbIdentifiersToLink.getLink(identifier), openBrowser, identifier.qualifier != null && identifier.qualifier.equals("identity")));
        }
    }
}
