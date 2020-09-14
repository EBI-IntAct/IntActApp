package uk.ac.ebi.intact.app.internal.ui.components.buttons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class IButton extends JButton  {
    public MouseListener handMouseShape = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    };

    public IButton() {
        clearIconButton();
    }

    public IButton(ImageIcon icon) {
        super(icon);
        clearIconButton();
    }

    private void clearIconButton() {
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        addMouseListener(handMouseShape);
    }


}
