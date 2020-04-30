package uk.ac.ebi.intact.intactApp.internal.ui.components.legend.shapes;

import javax.swing.*;
import java.awt.*;

public class Ball extends JComponent {
    Color color;
    Color borderColor;
    int diameter;

    public Ball(Color color, int diameter) {
        this.color = color;
        this.borderColor = color;
        this.diameter = diameter - 4;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(diameter + 5, diameter + 5);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(2, 2, diameter, diameter);
        g2.setStroke(new BasicStroke(4));
        g2.setPaint(borderColor);
        g2.drawOval(2, 2, diameter, diameter);
    }

    public void setColor(Color color) {
        this.color = color;
        this.borderColor = color;
        repaint();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
}