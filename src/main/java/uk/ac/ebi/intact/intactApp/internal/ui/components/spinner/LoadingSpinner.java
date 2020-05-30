package uk.ac.ebi.intact.intactApp.internal.ui.components.spinner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadingSpinner extends JComponent implements ActionListener {
    private int startingIndex = 0;
    private static final Color[] colors = new Color[]{
            new Color(134, 56, 148, 64),
            new Color(134, 56, 148, 64),
            new Color(134, 56, 148, 64),
            new Color(134, 56, 148, 64),
//            new Color(66, 42, 146, 128),
//            new Color(66, 42, 146, 192),
//            new Color(66, 42, 146),
            new Color(134, 56, 148),
            new Color(134, 56, 148, 192),
            new Color(134, 56, 148, 128),
            new Color(134, 56, 148, 64)
    };
    private final Timer timer;


    public LoadingSpinner() {
        timer = new Timer(170, this);
        setVisible(false);
        setPreferredSize(new Dimension(50, 50));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Insets insets = getInsets();
        int w = getWidth();
        int h = getHeight();
        int diameter = Integer.min(w, h);
        int radius = diameter / 2;
        float root = (float) Math.sqrt(2) / 2;
        float[] xs = {0, root, 1, root, 0, -root, -1, -root};
        float[] ys = {1, root, 0, -root, -1, -root, 0, root};
        int dotDiameter = diameter / 5;
        int dotRadius = dotDiameter / 2;
        g2.translate(insets.left + w / 2, insets.top + h / 2);
        float scaleFactor = radius - dotRadius;
        int colorPointer = startingIndex;
        for (int i = 0; i < xs.length; i++) {
            colorPointer = colorPointer + 1 != colors.length ? colorPointer + 1 : 0;
            g2.setPaint(colors[colorPointer]);
            g2.fillOval(Math.round(xs[i] * scaleFactor - dotRadius), Math.round(ys[i] * scaleFactor - dotRadius), dotDiameter, dotDiameter);
        }
        g2.translate(-(insets.left + w / 2), -(insets.top + h / 2));
    }

    public void start() {
        setVisible(true);
        timer.start();
    }

    public void stop() {
        setVisible(false);
        timer.stop();
    }

    public void setSpeed(int millis) {
        timer.setDelay(millis);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        startingIndex = startingIndex + 1 != colors.length ? startingIndex + 1 : 0;
        repaint();
    }
}
