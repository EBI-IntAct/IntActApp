package uk.ac.ebi.intact.app.internal.ui.components.labels;

import javax.swing.*;

public class SelectableLabel extends JTextField {
    public SelectableLabel(String text) {
        super(text);
        this.setEditable(false);
        this.setBorder(null);
        this.setDragEnabled(false);
        this.setBackground(null);
    }
}
