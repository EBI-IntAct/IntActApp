package uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution;

import javax.swing.*;
import java.awt.*;

class Cell extends JPanel {
    public static final Color HIGHLIGHTED_COLOR = new Color(71, 0, 255, 34);
    private final JComponent component;

    public Cell(JComponent component) {
        this.component = component;
        setLayout(new OverlayLayout(this));
        setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        component.setAlignmentX(CENTER_ALIGNMENT);
        component.setAlignmentY(CENTER_ALIGNMENT);
        add(component);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (component != null) {
            component.setBackground(bg);
        }
    }

    public void highlight() {
        JPanel filter = new JPanel();
        filter.setBackground(HIGHLIGHTED_COLOR);
        add(filter);
        setComponentZOrder(filter, 1);
    }
}
