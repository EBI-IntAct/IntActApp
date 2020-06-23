package uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution;

import javax.swing.*;
import java.awt.*;

class Cell extends JPanel {

    private final JComponent component;

    public Cell(JComponent component) {
        this.component = component;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        component.setAlignmentX(CENTER_ALIGNMENT);
        component.setAlignmentY(CENTER_ALIGNMENT);
        add(component, BorderLayout.CENTER);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (component != null) {
            component.setBackground(bg);
        }
    }
}
