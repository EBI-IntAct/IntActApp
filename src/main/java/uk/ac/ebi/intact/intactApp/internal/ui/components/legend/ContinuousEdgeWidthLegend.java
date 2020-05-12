package uk.ac.ebi.intact.intactApp.internal.ui.components.legend;

import uk.ac.ebi.intact.intactApp.internal.ui.utils.PaintUtils;

import javax.swing.*;
import java.awt.*;

public class ContinuousEdgeWidthLegend extends JComponent {
    private final int beginWidth;
    private final int endWidth;
    private final int beginValue;
    private final int endValue;

    private int height;
    private int edgeLength = 30;
    private Color edgeColor = new Color(231, 90, 0);
    private Color transitionColor = new Color(edgeColor.getRed(), edgeColor.getGreen(), edgeColor.getBlue(), 70);
    private int valuesFontSize = 10;
    private Font valuesFont = new Font("SansSerif", Font.PLAIN, valuesFontSize);

    public ContinuousEdgeWidthLegend(double beginWidth, double endWidth, int beginValue, int endValue) {
        this.beginWidth = (int) beginWidth;
        this.endWidth = (int) endWidth;
        this.beginValue = beginValue;
        this.endValue = endValue;
        height = Math.max(this.beginWidth, this.endWidth);
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        g2.translate(insets.left, insets.top);
        {
            g2.setPaint(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setPaint(edgeColor);
            int halfHeight = height / 2;
            int endX = getWidth() - (insets.left + insets.right) - edgeLength;
            int beginY = halfHeight - beginWidth / 2;
            int endY = halfHeight - endWidth / 2;
            int beginDownY = beginY + beginWidth;
            int endDownY = endY + endWidth;

            g2.fillRect(0, beginY, edgeLength, beginWidth);
            g2.fillRect(endX, endY, edgeLength, endWidth);
            g2.setPaint(transitionColor);
            g2.fillPolygon(new Polygon(
                    new int[]{edgeLength, endX, endX, edgeLength},
                    new int[]{beginY, endY, endDownY, beginDownY},
                    4));
            g2.setPaint(edgeColor);
            PaintUtils.drawAlignedString(g2, Integer.toString(beginValue), valuesFont, new Point(edgeLength, beginDownY), PaintUtils.HAlign.RIGHT, PaintUtils.VAlign.BOTTOM);
            PaintUtils.drawAlignedString(g2, Integer.toString(endValue), valuesFont, new Point(endX, endDownY), PaintUtils.HAlign.LEFT, PaintUtils.VAlign.BOTTOM);
        }
        g2.translate(-insets.left, -insets.top);
    }

    @Override
    public Dimension getPreferredSize() {
        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        return new Dimension(3 * edgeLength, insets.top + height + valuesFontSize + insets.bottom);
    }

    public int getEdgeLength() {
        return edgeLength;
    }

    public void setEdgeLength(int edgeLength) {
        this.edgeLength = edgeLength;
    }

    public Color getEdgeColor() {
        return edgeColor;
    }

    public void setEdgeColor(Color edgeColor) {
        this.edgeColor = edgeColor;
        this.transitionColor = new Color(edgeColor.getRed(), edgeColor.getGreen(), edgeColor.getBlue(), 70);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        valuesFont = font.deriveFont((float) valuesFontSize);
    }


}
