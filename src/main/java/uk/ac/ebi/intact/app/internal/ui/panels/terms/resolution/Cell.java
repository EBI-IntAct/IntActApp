package uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution;

import javax.swing.*;
import java.awt.*;

class Cell extends JPanel {
    public static final Color HIGHLIGHTED_COLOR = new Color(71, 0, 255, 34);
    private final JComponent component;
    private final Font originalFont;

    public Cell(JComponent component) {
        this.component = component;
        originalFont = component.getFont();
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

    public void highlight(boolean highlight) {
        if (highlight) {
            Font font = originalFont.deriveFont(Font.BOLD + Font.ITALIC);
            component.setFont(font);
            for (Component innerComponent : component.getComponents()) {
                innerComponent.setFont(font);
            }
        } else {
            component.setFont(originalFont);
            for (Component innerComponent : component.getComponents()) {
                innerComponent.setFont(originalFont);
            }
        }
    }
}
