package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class VersionPanel extends JPanel {

    public VersionPanel() {
        try {
            URL resource = VersionPanel.class.getResource("/buildInfo.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.openConnection().getInputStream()));
            String version = br.readLine();
            String buildDate = br.readLine();
            add(new JLabel("Build #" + version + " made " + buildDate));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
