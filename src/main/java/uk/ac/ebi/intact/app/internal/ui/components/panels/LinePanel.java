package uk.ac.ebi.intact.app.internal.ui.components.panels;

import javax.swing.*;
import java.awt.*;

public class LinePanel extends JPanel {
    public LinePanel() {
        this(0,null);
    }

    public LinePanel(int gap) {
        this(gap, null);
    }

    public LinePanel(Color backgroundColor) {
        this(0, backgroundColor);
    }

    public LinePanel(int gap, Color backgroundColor) {
        setLayout(new FlowLayout(FlowLayout.LEFT, gap, 0));
        setAlignmentX(LEFT_ALIGNMENT);
        if (backgroundColor!= null) setBackground(backgroundColor);
    }
}
