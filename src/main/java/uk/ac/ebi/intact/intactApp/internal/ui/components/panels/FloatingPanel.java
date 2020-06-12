package uk.ac.ebi.intact.intactApp.internal.ui.components.panels;

import javax.swing.*;
import java.awt.*;

import static java.lang.Integer.*;

public class FloatingPanel extends JPanel {

    private final Dimension toPlaceSize;
    private JComponent toPlace;

    public FloatingPanel(JComponent toPlace) {
        this.toPlace = toPlace;
        toPlaceSize = toPlace.getPreferredSize();
        toPlace.setSize(toPlaceSize);
        add(toPlace);
    }


    @Override
    public Dimension getPreferredSize() {
        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        Dimension preferredSize = super.getPreferredSize();
        preferredSize.width = insets.left + max(toPlaceSize.width, preferredSize.width) + insets.right;
        preferredSize.height = insets.top + max(toPlaceSize.height, preferredSize.height) + insets.bottom;
        return preferredSize;
    }


    @Override
    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
        Rectangle clip = getVisibleRect();
        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        g.setColor(getBackground());
        g.fillRect(insets.left, insets.top, getWidth() - insets.left - insets.right, getHeight() - insets.top - insets.bottom);

        int y = max(insets.top, min(clip.y + (clip.height - toPlaceSize.height) / 2, getHeight() - toPlaceSize.height - insets.bottom));
        int x = clip.x + (clip.width - toPlaceSize.width) / 2;
        toPlace.setLocation(x, y);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (toPlace != null) {
            toPlace.setBackground(bg);
        }
    }

    public JComponent getToPlace() {
        return toPlace;
    }

    public void setToPlace(JComponent toPlace) {
        this.toPlace = toPlace;
    }
}

