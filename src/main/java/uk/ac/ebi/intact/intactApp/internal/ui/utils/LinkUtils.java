package uk.ac.ebi.intact.intactApp.internal.ui.utils;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.ui.components.JLink;

public class LinkUtils {
    public static JLink createSpecieLink(OpenBrowser openBrowser, long taxId) {
        return new JLink(
                "- TaxId: " + taxId,
                "https://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=" + taxId + "&lvl=3&lin=f&keep=1&srchmode=1&unlock",
                openBrowser);
    }

    public static JLink createIntactEdgeLink(OpenBrowser openBrowser, IntactEvidenceEdge iEEdge) {
        return new JLink(iEEdge.ac, "https://www.ebi.ac.uk/intact/interaction/" + iEEdge.ac, openBrowser);
    }
}
