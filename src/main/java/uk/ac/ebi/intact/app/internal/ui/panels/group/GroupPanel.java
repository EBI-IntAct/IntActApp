package uk.ac.ebi.intact.app.internal.ui.panels.group;

import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.swing.*;
import java.awt.*;

public class GroupPanel extends JPanel implements CytoPanelComponent2 {

    private final Manager manager;

    private static final Icon icon = IconUtils.createImageIcon("/IntAct/DIGITAL/Gradient_over_Transparent/favicon_32x32.ico");

    public GroupPanel(Manager manager) {
        this.manager = manager;
    }

    @Override
    public String getIdentifier() {
        return "uk.ac.ebi.intact.app.group";
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public CytoPanelName getCytoPanelName() {
        return CytoPanelName.EAST;
    }

    @Override
    public String getTitle() {
        return "Groups";
    }

    @Override
    public Icon getIcon() {
        return icon;
    }
}
