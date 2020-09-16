package uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution;

import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.utils.ComponentUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.event.ItemEvent;

import static uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution.TermColumn.SELECT;

public class LimitRow extends Row {
    public LimitRow(TermTable table) {
        super(null, table);
    }

    @Override
    protected void init() {
        selected = getTable().getResolver().manager.option.DEFAULT_INCLUDE_ALL_INTERACTORS.getValue();
        getTable().includeAdditionalInteractors = selected;
        JCheckBox selectionCheckBox = createSelectionCheckBox(false);
        selectionCheckBox.addActionListener(e -> getTable().includeAdditionalInteractors = selectionCheckBox.isSelected());
        addCell(selectionCheckBox, TermColumn.SELECT);
        JLabel label = new JLabel("Include additional " + (getTable().totalInteractors - getTable().rows.size()) + " matching interactors");
        label.setBorder(new EmptyBorder(5,5,5,0));
        Cell cell = new Cell(label);
        cell.setBackground(selected ? UIColors.xLightPink : UIColors.xLightGray);
        label.setHorizontalAlignment(JLabel.LEFT);
        label.setAlignmentX(LEFT_ALIGNMENT);
        add(cell, layoutHelper.right().expandBoth());
        cells.put(null, cell); // Accept cell for repainting when selected
        ComponentUtils.resizeHeight(cells.get(SELECT), getPreferredSize().height, ComponentUtils.SizeType.PREF);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        super.itemStateChanged(e);
        if (e.getStateChange() == ItemEvent.SELECTED) getTable().includeAdditionalInteractors = true;
        else if (e.getStateChange() == ItemEvent.DESELECTED) getTable().includeAdditionalInteractors = false;
    }
}
