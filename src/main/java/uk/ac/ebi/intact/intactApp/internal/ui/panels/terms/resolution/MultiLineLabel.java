package uk.ac.ebi.intact.intactApp.internal.ui.panels.terms.resolution;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class MultiLineLabel extends JTextPane {

    private Color backgroundColor = new Color(216, 216, 216);
    private int alignment = StyleConstants.ALIGN_CENTER;
    private boolean init;

    public MultiLineLabel(String text) {
        super();
        setBorder(new EmptyBorder(0,0,0,0));
        setText(text);
        setEditable(false);
        setBackground(new Color(0,0,0,0));
        setOpaque(false);
        init = true;
    }

    private void styleText() {
        StyledDocument doc = getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setAlignment(style, alignment);
        StyleConstants.setBackground(style, backgroundColor);
        doc.setParagraphAttributes(0, doc.getLength(), style, false);
    }


    @Override
    public void setText(String t) {
        super.setText(t);
        styleText();
    }

    @Override
    public Color getBackground() {
        return backgroundColor;
    }
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (init) {
            this.backgroundColor = bg;
            styleText();
        }
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        if (init) {
            this.alignment = alignment;
            styleText();
        }
    }
}
