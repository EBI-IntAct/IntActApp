package uk.ac.ebi.intact.app.internal.ui.components.panels;

import javax.swing.*;
import java.awt.*;

public class LimitExceededPanel extends JPanel {

    public LimitExceededPanel(String limitOfWhat, String limitedState, int limit, String solution) {
        this(limitOfWhat, limitedState, limit, solution, JLabel.CENTER);
    }

    public LimitExceededPanel(String limitOfWhat, String limitedState, int limit, String solution, int horizontalAlignment) {
        setLayout(new GridLayout(2, 1));
        Font font = getFont().deriveFont(15f);
        JLabel label = new JLabel(String.format("More than %d %s %s", limit, limitOfWhat, limitedState));
        label.setForeground(Color.WHITE);
        label.setFont(font);
        label.setHorizontalAlignment(horizontalAlignment);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        add(label);
        JLabel label1 = new JLabel(String.format("For details on other %s, please %s", limitOfWhat, solution));
        label1.setFont(font);
        label1.setForeground(Color.WHITE);
        label1.setHorizontalAlignment(horizontalAlignment);
        label1.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        add(label1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Insets insets = new Insets(0, 0, 0, 0);
        insets = getInsets(insets);
        int height = getHeight() - insets.top - insets.bottom;
//            g2.setPaint(new LinearGradientPaint(0, 0, 0, height,
//                    new float[]{0.0f, 1f},
//                    new Color[]{new Color(66, 42, 146), new Color(134, 56, 148),}));
        g2.setPaint(new Color(161, 135, 184));
        g2.fillRoundRect(insets.left, insets.top, getWidth() - insets.left - insets.right, height, 10, 10);
    }

}