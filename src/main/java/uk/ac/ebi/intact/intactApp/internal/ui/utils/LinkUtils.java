package uk.ac.ebi.intact.intactApp.internal.ui.utils;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.ui.components.JLink;

public class LinkUtils {
    public static JLink createSpecieLink(long taxId, OpenBrowser openBrowser) {
        return new JLink(
                "(" + taxId + ")",
                "https://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=" + taxId + "&lvl=3&lin=f&keep=1&srchmode=1&unlock",
                openBrowser);
    }
}
