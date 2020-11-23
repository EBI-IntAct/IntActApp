package uk.ac.ebi.intact.app.internal.ui.panels.about;

import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.components.labels.HTMLLabel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.VersionPanel;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

public class AboutPanel extends VerticalPanel {
    public AboutPanel(Manager manager) {
        super();
        String content = null;
        URL resource = VersionPanel.class.getResource("/html/about.html");
        URL intactLogo = VersionPanel.class.getResource("/IntAct/DIGITAL/ICON_PNG/SmallLogo.png");
        URL emblLogo = VersionPanel.class.getResource("/EMBL_EBI-logo.png");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.openConnection().getInputStream()));
            content = br.lines().collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (content != null) {
            HTMLLabel about = new HTMLLabel(String.format(content, intactLogo, emblLogo));
            about.enableHyperlinks(manager);
            about.setBorder(new EmptyBorder(5,5,5,5));
            about.setBackground(Color.white);
            this.add(about);
        }
    }
}
