package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.identifiers.panels;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.Identifier;
import uk.ac.ebi.intact.app.internal.ui.components.labels.SelectableLabel;

import java.awt.*;
import java.util.List;

public class IntactIdentifierPanel extends IdentifierPanel {
    protected IntactIdentifierPanel(List<Identifier> identifiers, OpenBrowser openBrowser) {
        super(identifiers, openBrowser);
    }

    @Override
    protected void fillContent() {
        for (Identifier identifier : identifiers) {
            SelectableLabel label = new SelectableLabel(identifier.id);
            label.setForeground(Color.GRAY);
            label.setToolTipText("Not yet supported");
            addContent(label);
        }
    }
}
