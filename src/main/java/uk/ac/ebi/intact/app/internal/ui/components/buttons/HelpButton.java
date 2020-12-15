package uk.ac.ebi.intact.app.internal.ui.components.buttons;

import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.components.labels.HTMLLabel;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HelpButton extends IButton {
    private static final ImageIcon helpIcon = IconUtils.createImageIcon("/Buttons/help.png", 15, 15);
    private JPopupMenu popupMenu = new JPopupMenu();
    private final HTMLLabel message;
    private String title;
    private String helpText;


    public HelpButton(Manager manager, String title, String helpText) {
        super(helpIcon);
        this.title = title;
        this.helpText = "<html>" + helpText + "</html>";
        this.setBorder(new EmptyBorder(0, 5, 0, 0));
        message = new HTMLLabel(this.helpText);
        message.setSize(new Dimension(300, 10));
        message.setPreferredSize(new Dimension(300, message.getPreferredSize().height));
        message.enableHyperlinks(manager);
        message.setBorder(new EmptyBorder(0,5,0,5));
        popupMenu.add(message);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popupMenu.show(HelpButton.this, e.getX(), e.getY());
            }
        });
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
        this.message.setText(this.helpText);
    }
}
