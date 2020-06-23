package uk.ac.ebi.intact.app.internal.ui.components.panels;

import javax.swing.*;
import java.awt.*;

import static java.lang.Integer.*;

public class FloatingPanel extends JPanel {

    private final Dimension toPlaceSize;
    private JComponent toPlace;
    private boolean expandHoriz = false;
    private boolean expandVert = false;

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

        int width, height, x, y;
        if (expandHoriz) {
            width = clip.width;
            x = clip.x;
        } else {
            width = toPlaceSize.width;
            x = clip.x + (clip.width - toPlaceSize.width) / 2;
        }

        if (expandVert) {
            height = clip.height;
            y = clip.y;
        } else {
            height = toPlaceSize.height;
            y = max(insets.top, min(clip.y + (clip.height - toPlaceSize.height) / 2, getHeight() - toPlaceSize.height - insets.bottom));
        }

        toPlace.setBounds(x, y, width, height);
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

    public void expandHoriz() {
        this.expandHoriz = true;
    }

    public void expandVert() {
        this.expandVert = true;
    }

    public void noExpand() {
        this.expandVert = false;
        this.expandHoriz = false;
    }
}

