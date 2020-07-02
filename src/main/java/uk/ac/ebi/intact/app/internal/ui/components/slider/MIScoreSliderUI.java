package uk.ac.ebi.intact.app.internal.ui.components.slider;

import uk.ac.ebi.intact.app.internal.model.styles.SummaryStyle;
import uk.ac.ebi.intact.app.internal.ui.utils.PaintUtils;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * UI delegate for the RangeSlider component.  RangeSliderUI paints two thumbs,
 * one for the lower value and one for the upper value.
 */
public class MIScoreSliderUI extends RangeSliderUI {

    public static Color[] colors = new Color[(SummaryStyle.colors.length - 1) * 2 - 1];
    public static float[] floats = new float[(SummaryStyle.colors.length - 1) * 2 - 1];

    public MIScoreSliderUI(RangeSlider b) {
        super(b);
    }

    static {
        for (int i = 0; i < SummaryStyle.colors.length - 1; i++) {
            Color color = SummaryStyle.colors[i];
            colors[2 * i] = color;
            floats[2 * i] = (i / 10.0f);
            if (i < SummaryStyle.colors.length - 2) {
                colors[2 * i + 1] = SummaryStyle.colors[i + 1];
                floats[2 * i + 1] = (i / 10.0f) + 0.001f;
            }
        }
    }

    /**
     * Calculates the track rectangle.
     */
    @Override
    protected void calculateTrackRect() {
        super.calculateTrackRect();
        trackRect.y = 1;
    }


    /**
     * Returns the size of a thumb.
     */
    @Override
    protected Dimension getThumbSize() {
        return new Dimension(36, 36);
    }

    public void paintFocus(Graphics g) {
    }

    /**
     * Paints the track.
     */
    @Override
    public void paintTrack(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        Rectangle trackBounds = trackRect;
        int unselectedHeight = 4;
        int selectedHeight = 12;
        int halfThumb = thumbRect.width / 2;

        RenderingHints qualityHints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHints(qualityHints);


        // Determine position of selected range by moving from the middle
        // of one thumb to the other.
        int lowerX = thumbRect.x + halfThumb;
        int upperX = upperThumbRect.x + (upperThumbRect.width / 2);

        Point2D start = new Point2D.Float(halfThumb, 0);
        Point2D end = new Point2D.Float(trackRect.width + halfThumb, 0);

        LinearGradientPaint p = new LinearGradientPaint(start, end, floats, colors);

        // Save color and shift position.
        Color oldColor = g.getColor();
        int cy = slider.getHeight() / 4;
        g.translate(0, cy);

        g.setPaint(p);
        g.fillRoundRect(halfThumb, -unselectedHeight / 2, trackBounds.width, unselectedHeight, unselectedHeight, unselectedHeight);

        g.setPaint(new Color(84, 84, 84, 100));
        g.fillRoundRect(halfThumb, -unselectedHeight / 2, trackBounds.width, unselectedHeight, unselectedHeight, unselectedHeight);

        g.setPaint(p);
        g.fillRect(lowerX, -selectedHeight / 2, upperX - lowerX, selectedHeight);

        g.setPaint(slider.getForeground());
        g.setStroke(new BasicStroke(2));
        g.drawRect(lowerX, -selectedHeight / 2, upperX - lowerX, selectedHeight);

        g.setStroke(new BasicStroke());
        g.setColor(oldColor);

    }


    /**
     * Paints the thumb for the lower value using the specified graphics object.
     */
    protected void paintLowerThumb(Graphics g) {
        paintOneThumb(g, thumbRect);
    }

    /**
     * Paints the thumb for the upper value using the specified graphics object.
     */
    protected void paintUpperThumb(Graphics g) {
        paintOneThumb(g, upperThumbRect);
    }

    private void paintOneThumb(Graphics g, Rectangle knobBounds) {
        int w = knobBounds.width / 2;
        int h = knobBounds.height / 2;

        // Create graphics copy.
        Graphics2D g2d = (Graphics2D) g.create();

        // Create default thumb shape.
        Shape thumbShape = createThumbShape(w - 2, h - 2);

        // Draw thumb.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(knobBounds.x + w / 2, knobBounds.y - h / 2);

        g2d.setColor(getColorAtPosition(getPosition(knobBounds)));
        g2d.fill(thumbShape);

        g2d.setPaint(slider.getForeground());
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(thumbShape);
        g2d.setStroke(new BasicStroke());

        Rectangle labelSpace = new Rectangle(-w / 2, h, knobBounds.width, h);
        g2d.setPaint(slider.getBackground());
        g2d.fillRect(labelSpace.x, labelSpace.y, labelSpace.width, labelSpace.height);
        g2d.setPaint(Color.BLACK);
        PaintUtils.drawCenteredString(g2d, String.format("%.2f", getPosition(knobBounds)), labelSpace, slider.getFont());

        // Dispose graphics.
        g2d.dispose();
    }


    private float getPosition(Rectangle thumbRect) {
        if (thumbRect == this.thumbRect) {
            return slider.getValue() / 100f;
        } else {
            return (slider.getValue() + slider.getExtent()) / 100f;
        }
    }

    static Color getColorAtPosition(float position) {
        if (position <= 0)
            return colors[0];

        for (int i = 0; i < floats.length; i++) {
            if (floats[i] >= position) {
                return colors[i - 1];
            }
        }
        return colors[colors.length - 1];
    }
}
