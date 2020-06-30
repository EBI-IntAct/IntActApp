package uk.ac.ebi.intact.app.internal.ui.components.buttons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ToggleSwitch extends AbstractButton implements MouseListener {
    private boolean activated;
    private Color accentColor;
    private final Color unactivatedColor = new Color(177, 177, 177);
    private final Color buttonColor = new Color(251, 251, 251);
    private final int height = 15;
    private final int width = 30;

    public ToggleSwitch(boolean activated) {
        this(activated, new Color(34, 83, 157));
    }

    public ToggleSwitch(boolean activated, Color accentColor) {
        this.activated = activated;
        this.accentColor = accentColor;
        this.addMouseListener(this);
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
        repaint();
    }

    public boolean toggle() {
        activated = !activated;
        repaint();
        fireStateChanged();
        return activated;
    }

    public Color getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(Color accentColor) {
        this.accentColor = accentColor;
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        g2.translate(insets.left, insets.top);

        int delta = 2;
        int diameter = height - 2 * delta;

        g2.setPaint((activated) ? accentColor : unactivatedColor);
        g2.fillRoundRect(0, 0, width, height, height, height);

        int x = activated ? width - delta - diameter : delta;
        g2.setPaint(new Color(62, 62, 62, 20));
        g2.fillOval(x, delta + 1, diameter, diameter);

        g2.setPaint(buttonColor);
        g2.fillOval(x, delta, diameter, diameter);

        g2.translate(-insets.left, -insets.top);
    }

    @Override
    public Dimension getPreferredSize() {
        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        return new Dimension(insets.left + width + insets.right, insets.top + height + insets.bottom);
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        toggle();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public String toString() {
        return "Switch " + (activated ? "On" : "Off");
    }
}
