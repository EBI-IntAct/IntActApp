package uk.ac.ebi.intact.app.internal.ui.components.buttons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class IButton extends JButton  {
    public MouseListener handMouseShape = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    };

    public IButton() {
        cleanIconButton();
    }

    public IButton(ImageIcon icon) {
        super(icon);
        cleanIconButton();
    }

    private void cleanIconButton() {
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        addMouseListener(handMouseShape);
    }


}
