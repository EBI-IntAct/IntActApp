package uk.ac.ebi.intact.app.internal.ui.components.buttons;

import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class DocumentedButton extends JPanel {

    public DocumentedButton(Manager manager, String title, String helpText, ActionListener listener) {
        this.setLayout(new GridBagLayout());
        EasyGBC c = new EasyGBC();
        JButton actionButton = new JButton(title);
        actionButton.addActionListener(listener);
        add(actionButton, c.expandHoriz());
        add(new HelpButton(manager, helpText), c.right().noExpand());
    }
}
