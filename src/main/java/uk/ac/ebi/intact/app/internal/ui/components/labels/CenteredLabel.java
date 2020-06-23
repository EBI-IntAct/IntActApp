package uk.ac.ebi.intact.app.internal.ui.components.labels;

import javax.swing.*;
import java.awt.*;

public class CenteredLabel extends JLabel {
    public CenteredLabel(String text) {
        super(text);
        this.setHorizontalAlignment(CENTER);
        this.setVerticalAlignment(CENTER);
        this.setAlignmentX(CENTER_ALIGNMENT);
        this.setAlignmentY(CENTER_ALIGNMENT);
    }

    public CenteredLabel(String text, float fontSize, Color color) {
        this(text);
        setFont(getFont().deriveFont(fontSize));
        setForeground(color);
    }
}