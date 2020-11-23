package uk.ac.ebi.intact.app.internal.ui.components.labels;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public class HTMLLabel extends JEditorPane implements HyperlinkListener {
    private Manager manager;
    private OpenBrowser openBrowser;
    private static final Font DEFAULT_FONT;

    static {
        Font font = UIManager.getFont("Label.font");
        DEFAULT_FONT = (font != null) ? font : new Font("SansSerif", Font.PLAIN, 11);
    }

    public HTMLLabel(String text) {
        super();
        this.setContentType("text/html");
        this.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        this.setFont(DEFAULT_FONT);
        this.setText(text);
        this.addHyperlinkListener(this);
        this.setEditable(false);
        this.setBorder(null);
        this.setDragEnabled(false);
        this.setBackground(null);
    }

    public void enableHyperlinks(Manager manager) {
        this.manager = manager;
        this.openBrowser = manager.utils.getService(OpenBrowser.class);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
            System.out.println(openBrowser);
            if (openBrowser != null) {
                openBrowser.openURL(e.getURL().toString());
            } else {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
