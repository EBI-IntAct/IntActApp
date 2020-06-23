package uk.ac.ebi.intact.app.internal.ui.components.panels;

import javax.swing.*;
import java.awt.*;

public class LinePanel extends JPanel {
    public LinePanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setAlignmentX(LEFT_ALIGNMENT);
    }

    public LinePanel(int gap) {
        setLayout(new FlowLayout(FlowLayout.LEFT, gap, 0));
        setAlignmentX(LEFT_ALIGNMENT);
    }

    public LinePanel(Color backgroundColor) {
        this();
        setBackground(backgroundColor);
    }
}
