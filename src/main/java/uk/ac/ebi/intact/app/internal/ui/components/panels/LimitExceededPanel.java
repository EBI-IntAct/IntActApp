package uk.ac.ebi.intact.app.internal.ui.components.panels;

import javax.swing.*;
import java.awt.*;

public class LimitExceededPanel extends JPanel {

    private String limitOfWhat;
    private String solution;
    private String limitedState;
    private int limit;
    private Font font;
    private int horizontalAlignment;

    public LimitExceededPanel(String limitOfWhat, String limitedState, int limit, String solution) {
        this(limitOfWhat, limitedState, limit, solution, JLabel.CENTER);
    }

    public LimitExceededPanel(String limitOfWhat, String limitedState, int limit, String solution, int horizontalAlignment) {
        this.limit = limit;
        this.limitOfWhat = limitOfWhat;
        this.limitedState = limitedState;
        this.solution = solution;
        this.horizontalAlignment = horizontalAlignment;
        setLayout(new GridLayout(2, 1));
        font = getFont().deriveFont(15f);
    }

    private void setupLabels() {
        removeAll();
        JLabel label = new JLabel(String.format("More than %d %s %s", this.limit, this.limitOfWhat, this.limitedState));
        label.setForeground(Color.WHITE);
        label.setFont(font);
        label.setHorizontalAlignment(horizontalAlignment);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        add(label);
        JLabel label1 = new JLabel(String.format("For details on other %s, please %s", this.limitOfWhat, this.solution));
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
        g2.setPaint(new Color(161, 135, 184));
        g2.fillRoundRect(insets.left, insets.top, getWidth() - insets.left - insets.right, height, 10, 10);
    }

    public String getLimitOfWhat() {
        return limitOfWhat;
    }

    public void setLimitOfWhat(String limitOfWhat) {
        this.limitOfWhat = limitOfWhat;
        setupLabels();
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
        setupLabels();
    }

    public String getLimitedState() {
        return limitedState;
    }

    public void setLimitedState(String limitedState) {
        this.limitedState = limitedState;
        setupLabels();
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
        setupLabels();
    }

    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        setupLabels();
    }
}