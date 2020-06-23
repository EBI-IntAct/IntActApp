package uk.ac.ebi.intact.app.internal.ui.components.legend;

import uk.ac.ebi.intact.app.internal.model.styles.utils.StyleMapper;

import javax.swing.*;
import java.awt.*;


public class EdgeLegend extends JComponent {
    public enum LineType {
        SOLID,
        DASHED
    }

    protected int length = 30;
    protected int thickness = 4;
    protected Paint paint = StyleMapper.edgeTypeToPaint.get("colocalization");
    protected LineType lineType = LineType.SOLID;

    private EdgeLegend() {
        Dimension size = new Dimension(length, 20);
        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(size);
        setSize(size);
    }

    public EdgeLegend(int thickness) {
        this();
        this.thickness = thickness;
    }

    public EdgeLegend(Paint paint) {
        this();
        this.paint = paint;
    }

    public EdgeLegend(LineType lineType) {
        this();
        this.lineType = lineType;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(paint);
        switch (lineType) {
            case SOLID:
                g2.setStroke(new BasicStroke(thickness));
                break;
            case DASHED:
                g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[]{6.0f, 5.0f}, 0));
                break;
        }

        int halfHeight = getHeight() / 2;
        g2.drawLine(0, halfHeight, getWidth(), halfHeight);
    }


    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public LineType getLineType() {
        return lineType;
    }

    public void setLineType(LineType lineType) {
        this.lineType = lineType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
        Dimension size = new Dimension(length, 20);
        setMinimumSize(size);
        setPreferredSize(size);
        setMaximumSize(size);
        setSize(size);
    }
}
