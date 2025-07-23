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
        c.insets = new Insets(5,0,0,0);
        JButton actionButton = new JButton(title);
        actionButton.addActionListener(listener);
        add(actionButton, c.expandBoth());
        add(new HelpButton(manager, helpText), c.right().noExpand());
    }
}
