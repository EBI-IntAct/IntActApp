package uk.ac.ebi.intact.intactApp.internal.ui.components.panels;

import javax.swing.*;
import java.awt.*;

public class VerticalPanel extends JPanel {

    public VerticalPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public VerticalPanel(Color backgroundColor) {
        this();
        setBackground(backgroundColor);
    }
}
