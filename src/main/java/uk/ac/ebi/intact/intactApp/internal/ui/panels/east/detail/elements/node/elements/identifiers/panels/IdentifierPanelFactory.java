package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.identifiers.panels;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.Identifier;

import java.util.List;

public abstract class IdentifierPanelFactory {
    public static IdentifierPanel createPanel(List<Identifier> identifiers, OpenBrowser openBrowser) {
        switch (identifiers.get(0).databaseName) {
            case "go":
                return new GOIdentifierPanel(identifiers, openBrowser);
            case "interpro":
                return new InterProIdentifierPanel(identifiers, openBrowser);
            case "reactome":
                return new ReactomeIdentifierPanel(identifiers, openBrowser);
            default:
                return new IdentifierPanel(identifiers, openBrowser);
        }
    }
}