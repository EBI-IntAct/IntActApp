package uk.ac.ebi.intact.intactApp.internal.ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TextAreaRenderer extends JTextArea implements TableCellRenderer {
    static int PADDING = 10;

    public TextAreaRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        setText((String) value);//or something in value, like value.getNote()...
        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
        if (table.getRowHeight(row) != getPreferredSize().height + PADDING) {
            table.setRowHeight(row, getPreferredSize().height + PADDING);
        }
        return this;
    }
} 
