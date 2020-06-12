package uk.ac.ebi.intact.intactApp.internal.ui.components.legend.shapes;

import javax.swing.*;
import java.awt.*;


public abstract class AbstractNodeShape extends JComponent {
    private int originalWidth;
    private int originalHeight;
    protected int width;
    protected int height;
    protected Color color;
    private int borderThickness = 0;
    private Color borderColor;

    public AbstractNodeShape(int width, int height, Color color) {
        this.originalWidth = width;
        this.width = width;
        this.originalHeight = height;
        this.height = height;
        this.color = color;
        this.borderColor = color;
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(color);
        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        g2.translate(insets.left + borderThickness / 2 + (getWidth() - originalWidth) / 2, insets.top + borderThickness / 2 + (getHeight() - originalHeight) / 2);
        g2.fill(getShape());
        if (borderThickness > 0) {
            g2.setStroke(new BasicStroke(borderThickness));
            g2.setPaint(borderColor);
            g2.draw(getShape());
        }
    }

    abstract protected Shape getShape();

    @Override
    public Dimension getPreferredSize() {
        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        return new Dimension(insets.left + originalWidth + insets.right + borderThickness / 2, insets.top + originalHeight + insets.bottom + borderThickness / 2);
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
        this.width = originalWidth - borderThickness;
        this.height = originalHeight - borderThickness;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
