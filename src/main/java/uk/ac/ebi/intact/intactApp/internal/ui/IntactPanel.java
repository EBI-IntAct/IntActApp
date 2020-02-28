package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.utils.OpenCyBrowser;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * Displays string information
 *
 * @author Scooter Morris
 */
public class IntactPanel extends JPanel {
    final IntactNetwork intactNetwork;
    final IntactNode sNode;
    final OpenBrowser openBrowser;

    public IntactPanel(final OpenBrowser openBrowser, final IntactNetwork intactNetwork, final CyNode node) {
        this.intactNetwork = intactNetwork;
        this.sNode = new IntactNode(intactNetwork, node);
        this.openBrowser = openBrowser;

        setLayout(new GridBagLayout());

        EasyGBC c = new EasyGBC();

        // Add the crosslinks
        {
            JEditorPane textArea = new JEditorPane("text/html", null);
            textArea.addHyperlinkListener(new MyHLinkListener());
            textArea.setBackground(getBackground());
            textArea.setEditable(false);
            String message = "<h3 style=\"margin-left: 5px;margin-bottom: 0px;\">CrossLinks</h3>";
            message += "<table style=\"margin-left: 10px;margin-top: 0px;\">";
            if (sNode.haveUniprot()) {
                message += "<tr><td>Uniprot: </td>";
                message += "<td><a href=\"" + sNode.getUniprotURL() + "\">" + sNode.getUniprot() + "</a></td></tr>";
            }
            if (sNode.haveGeneCard()) {
                message += "<tr><td>GeneCard: </td>";
                message += "<td><a href=\"" + sNode.getGeneCardURL() + "\">" + sNode.getUniprot() + "</a></td></tr>";
            }

            if (sNode.haveCompartments()) {
                message += "<tr><td>Compartments: </td>";
                message += "<td><a href=\"" + sNode.getCompartmentsURL() + "\">" + sNode.getCompartments() + "</a></td></tr>";
            }

            if (sNode.haveTissues()) {
                message += "<tr><td>Tissues: </td>";
                message += "<td><a href=\"" + sNode.getTissuesURL() + "\">" + sNode.getTissues() + "</a></td></tr>";
            }

            if (sNode.haveDisease()) {
                message += "<tr><td>Diseases: </td>";
                message += "<td><a href=\"" + sNode.getDiseaseURL() + "\">" + sNode.getDisease() + "</a></td></tr>";
            }

            if (sNode.havePharos()) {
                message += "<tr><td>Pharos: </td>";
                message += "<td><a href=\"" + sNode.getPharosURL() + "\">" + sNode.getPharos() + "</a></td></tr>";
            }

            if (sNode.havePubChem()) {
                message += "<tr><td>PubChem: </td>";
                message += "<td><a href=\"" + sNode.getPubChemURL() + "\">" + sNode.getPubChem() + "</a></td></tr>";
            }

            message += "</table>";
            textArea.setText(message);
            add(textArea, c.down().anchor("west").noExpand());
            add(new JPanel(), c.down().anchor("west").expandVert());
        }

        {
            // JEditorPane textArea = new JEditorPane("text/html", null);
            // textArea.setBackground(getBackground());
            // textArea.setEditable(false);
            // String message = "<html><div style=\"margin-left: 5px; margin-right: 5px; width=100px;\">"+sNode.getDescription()+"</div></html>";
            // JLabel label = new JLabel(message);
            JTextArea textArea = new JTextArea(sNode.getDescription(), 2, 20);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setEditable(false);
            textArea.setBackground(getBackground());
            // textArea.setText(message);
            // textArea.setPreferredSize(new Dimension(100,100));
            add(textArea, c.anchor("west").noExpand());
        }

        // Now add our image
        Image img = sNode.getStructureImage();
        if (img != null) {
            Image scaledImage = img.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            JLabel label = new JLabel(new ImageIcon(scaledImage));
            // label.setPreferredSize(new Dimension(100,100));
            // label.setMinimumSize(new Dimension(100,100));
            add(label, c.down().anchor("west").noExpand());
        }

    }

    // JEditorPane textArea = new JEditorPane("text/html", null);
    // textArea.setEditable(false);
    // textArea.addHyperlinkListener(new HyperlinkListener() {

    class MyHLinkListener implements HyperlinkListener {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (intactNetwork.getManager().haveCyBrowser()) {
                    OpenCyBrowser.openURL(intactNetwork.getManager(), "StringApp", e.getURL().toString());
                } else {
                    openBrowser.openURL(e.getURL().toString());
                }
            }
        }
    }

}
