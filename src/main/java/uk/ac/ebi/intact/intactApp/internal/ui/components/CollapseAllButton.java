package uk.ac.ebi.intact.intactApp.internal.ui.components;

import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

public class CollapseAllButton extends JButton implements ActionListener {
    private static final ImageIcon up = IconUtils.createImageIcon("/Buttons/DoubleUp.png");
    private static final ImageIcon down = IconUtils.createImageIcon("/Buttons/DoubleDown.png");

    private boolean isUp;
    private final Collection<? extends CollapsablePanel> panels;

    public CollapseAllButton(boolean isUp, Collection<? extends CollapsablePanel> panels) {
        this.isUp = isUp;
        this.panels = panels;
        setBorder(new EmptyBorder(0,0,0,0));
        setOpaque(false);
        setIcon(isUp ? up : down);
        setToolTipText(isUp ? "Collapse all" : "Expand all");
        addActionListener(this);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (isUp) {
            for (CollapsablePanel panel : panels) {
                panel.collapse();
            }
            setIcon(down);
            setToolTipText("Expand all");
            isUp = false;
        } else {
            for (CollapsablePanel panel : panels) {
                panel.expand();
            }
            setIcon(up);
            setToolTipText("Collapse all");
            isUp = true;
        }
    }

    public boolean isExpanded() {
        return isUp;
    }

    public void setOnExpandAll(boolean expandAll) {
        if (expandAll) {
            setIcon(down);
            setToolTipText("Expand all");
            isUp = false;
        } else {
            setIcon(up);
            setToolTipText("Collapse all");
            isUp = true;
        }
    }
}
