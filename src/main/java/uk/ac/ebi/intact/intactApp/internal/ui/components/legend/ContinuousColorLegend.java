package uk.ac.ebi.intact.intactApp.internal.ui.components.legend;

import uk.ac.ebi.intact.intactApp.internal.ui.utils.PaintUtils;

import javax.swing.*;
import java.awt.*;

public class ContinuousColorLegend extends JComponent {

    private Color[] gradientColors;
    private float[] gradientPositions;
    private int maxTicksNumber;

    private String ticksFormat = "%.1f";
    private final int trackToTicksGap = 3;
    private int trackHeight = 15;
    private int ticksFontSize = 10;
    private int tickWidth;
    private Rectangle clip;

    public ContinuousColorLegend(Color[] gradientColors, float[] gradientPositions, int maxTicksNumber) {
        this.gradientColors = gradientColors;
        this.gradientPositions = gradientPositions;
        this.maxTicksNumber = maxTicksNumber;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        g2.translate(insets.left, insets.top);

        calculateMeasures(g2, insets);
        paintBackground(g2);
        paintTrack(g2);
        paintTicks(g2);

        g2.translate(-insets.left, -insets.top);
    }

    private void calculateMeasures(Graphics2D g2, Insets insets) {
        clip = g2.getClipBounds();
        clip.width = getWidth() - (insets.left + insets.right);
        FontMetrics metrics = g2.getFontMetrics(getTickFont(g2));
        tickWidth = metrics.stringWidth("0.0");
    }

    private void paintBackground(Graphics2D g2) {
        g2.setPaint(getBackground());
        g2.fill(g2.getClip());
    }


    protected void paintTrack(Graphics2D g2) {
        g2.setPaint(new LinearGradientPaint(0, 0, clip.width, 0, gradientPositions, gradientColors));
        g2.fillRoundRect(0, 0, clip.width, trackHeight, trackHeight, trackHeight);
        g2.setPaint(Color.LIGHT_GRAY);
        g2.drawRoundRect(0, 0, clip.width, trackHeight, trackHeight, trackHeight);
    }

    private void paintTicks(Graphics2D g2) {
        float min = gradientPositions[0];
        float max = gradientPositions[gradientPositions.length - 1];
        float range = max - min;
        int tickNumber = Math.min(Math.floorDiv(clip.width, tickWidth + 10), maxTicksNumber);
        tickNumber = PaintUtils.getFloorDivisor(tickNumber, maxTicksNumber);
        float delta = range / tickNumber;
        float valueToXPositionFactor = clip.width / range;
        int tickHeight = trackHeight + trackToTicksGap;

        g2.setPaint(getForeground());
        PaintUtils.drawAlignedString(g2, String.format("%d", (int) min), getTickFont(g2), new Point(0, tickHeight), PaintUtils.HAlign.LEFT, PaintUtils.VAlign.BOTTOM);
        for (float i = min + delta; i < max; i += delta) {
            PaintUtils.drawAlignedString(g2, String.format(ticksFormat, i), getTickFont(g2), new Point((int) (i * valueToXPositionFactor), tickHeight), PaintUtils.HAlign.CENTER, PaintUtils.VAlign.BOTTOM);
        }
        PaintUtils.drawAlignedString(g2, String.format("%d", (int) max), getTickFont(g2), new Point(clip.width, tickHeight), PaintUtils.HAlign.RIGHT, PaintUtils.VAlign.BOTTOM);
    }

    private Font getTickFont(Graphics2D g2) {
        return new Font(g2.getFont().getFontName(), Font.PLAIN, ticksFontSize);
    }


    @Override
    public Dimension getPreferredSize() {
        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        return new Dimension(100 + insets.left + insets.right, trackHeight + trackToTicksGap + ticksFontSize + insets.top + insets.bottom);
    }

    public String getTicksFormat() {
        return ticksFormat;
    }

    public void setTicksFormat(String ticksFormat) {
        this.ticksFormat = ticksFormat;
    }

    public int getTicksFontSize() {
        return ticksFontSize;
    }

    public void setTicksFontSize(int ticksFontSize) {
        this.ticksFontSize = ticksFontSize;
    }

    public int getTrackHeight() {
        return trackHeight;
    }

    public void setTrackHeight(int trackHeight) {
        this.trackHeight = trackHeight;
    }
}
