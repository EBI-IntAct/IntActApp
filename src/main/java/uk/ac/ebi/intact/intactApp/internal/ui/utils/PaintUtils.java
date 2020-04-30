package uk.ac.ebi.intact.intactApp.internal.ui.utils;

import java.awt.*;
import java.util.SortedSet;
import java.util.TreeSet;

;

public class PaintUtils {
    public enum VAlign {
        TOP,
        MIDDLE,
        BOTTOM
    }

    public enum HAlign {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param g    The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    public static void drawAlignedString(Graphics g, String text, Font font, Point alignmentPoint, HAlign horizontalAlignment, VAlign verticalAlignment) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x, y;
        switch (horizontalAlignment) {
            case LEFT:
                x = alignmentPoint.x;
                break;
            default:
            case CENTER:
                x = alignmentPoint.x - metrics.stringWidth(text) / 2;
                break;
            case RIGHT:
                x = alignmentPoint.x - metrics.stringWidth(text);
                break;
        }

        switch (verticalAlignment) {
            case TOP:
                y = alignmentPoint.y;  // + metrics.getAscent()
                break;
            default:
            case MIDDLE:
                y = alignmentPoint.y + metrics.getAscent() / 2;
                break;
            case BOTTOM:
                y = alignmentPoint.y + metrics.getAscent();
                break;
        }
        g.setFont(font);
        g.drawString(text, x, y);
    }

    public static SortedSet<Integer> divisors(int n) {
        SortedSet<Integer> divisors = new TreeSet<>();
        divisors.add(1);
        double max = Math.sqrt(n);
        for (int i = 2; i <= max; i++) {
            if (n % i == 0) {
                divisors.add(i);
                divisors.add(n / i);
            }
        }
        divisors.add(n);
        return divisors;
    }

    public static int getFloorDivisor(int closestTo, int divisorOf) {
        for (int i = closestTo; i > 1; --i) {
            if (divisorOf % i == 0) {
                return i;
            }
        }
        return 1;
    }
}
