package uk.ac.ebi.intact.app.internal.ui.components.labels.center;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class CenteredLabel extends JTextPane {
    public CenteredLabel(String text) {
        this.setText(text);
        this.setEditable(false);
        this.setBackground(null);
        this.setBorder(null);

        this.setEditorKit(new CenteredEditor());
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);
        StyledDocument doc = (StyledDocument) this.getDocument();
        try {
            doc.insertString(0, text, attrs);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        doc.setParagraphAttributes(0, doc.getLength() - 1, attrs, false);
        this.setAlignmentX(CENTER_ALIGNMENT);
        this.setAlignmentY(CENTER_ALIGNMENT);
    }

    public CenteredLabel(String text, float fontSize, Color color) {
        this(text);
        setFont(getFont().deriveFont(fontSize));
        setForeground(color);
    }
}

