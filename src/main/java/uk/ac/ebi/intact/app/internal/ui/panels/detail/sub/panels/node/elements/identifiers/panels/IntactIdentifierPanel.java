package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.identifiers.panels;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.Identifier;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class IntactIdentifierPanel extends IdentifierPanel {
    protected IntactIdentifierPanel(List<Identifier> identifiers, OpenBrowser openBrowser) {
        super(identifiers, openBrowser);
    }

    @Override
    protected void fillContent() {
        for (Identifier identifier : identifiers) {
            JLabel label = new JLabel("<html>" + identifier.id + "</html>");
            label.setForeground(Color.GRAY);
            label.setToolTipText("Not yet supported");
            addContent(label);
        }
    }
}
