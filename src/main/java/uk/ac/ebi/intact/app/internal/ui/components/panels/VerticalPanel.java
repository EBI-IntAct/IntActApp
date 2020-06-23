package uk.ac.ebi.intact.app.internal.ui.components.panels;

import javax.swing.*;
import java.awt.*;

public class VerticalPanel extends JPanel {

    public VerticalPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
    }

    public VerticalPanel(Color backgroundColor) {
        this();
        setOpaque(true);
        setBackground(backgroundColor);
    }
}
