package uk.ac.ebi.intact.intactApp.internal.ui.panels.terms.resolution;

import uk.ac.ebi.intact.intactApp.internal.model.core.Interactor;
import uk.ac.ebi.intact.intactApp.internal.ui.components.diagrams.InteractorDiagram;
import uk.ac.ebi.intact.intactApp.internal.ui.components.labels.CenteredLabel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.ComponentUtils;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.terms.resolution.TermColumn.*;

class Row extends JPanel implements ItemListener {
    public static final Color SELECTED_COLOR = new Color(208, 196, 214);
    public static final Color UNSELECTED_COLOR = new Color(216, 216, 216);
    private final EasyGBC layoutHelper = new EasyGBC();
    final Interactor interactor;
    final TermTable table;
    final Map<TermColumn, Cell> cells = new HashMap<>();
    boolean selected;


    private final static Pattern speciesPattern = Pattern.compile("[A-Z][a-z.]+ [a-z]+");
    JCheckBox selectionCheckBox;
    InteractorDiagram diagram;


    public Row(Interactor interactor, TermTable table) {
        this.interactor = interactor;
        this.table = table;
        selected = table.resolver.selectedByDefault;
        init();
    }

    private void init() {
        this.setLayout(new GridBagLayout());
        this.setBackground(Color.WHITE);
        this.addMouseListener(mouseAdapter);
        layoutHelper.expandBoth().anchor("west");
        addCell(createSelectionCheckBox(), SELECT);
        addCell(createPreview(interactor), PREVIEW);
        addCell(createSpecies(interactor), SPECIES);
        addCell(new CenteredLabel(interactor.type), TYPE);
        addCell(new CenteredLabel(interactor.name), NAME);
        addCell(new CenteredLabel(interactor.fullName), DESCRIPTION);
        addCell(new CenteredLabel(interactor.interactionCount.toString()), NB_INTERACTIONS);
        addCell(new CenteredLabel(interactor.preferredId), ID);
        addCell(new CenteredLabel(interactor.ac), AC);

        ComponentUtils.resizeHeight(cells.get(SELECT), getPreferredSize().height, ComponentUtils.SizeType.PREF);
    }

    private JCheckBox createSelectionCheckBox() {
        selectionCheckBox = new JCheckBox();
        selectionCheckBox.setAlignmentX(CENTER_ALIGNMENT);
        selectionCheckBox.setSelected(selected);
        selectionCheckBox.addItemListener(this);
        selectionCheckBox.addItemListener(table);
        selectionCheckBox.addItemListener(table.resolver);
        return selectionCheckBox;
    }

    private JPanel createPreview(Interactor interactor) {
        JPanel cellContent = new VerticalPanel();

        cellContent.add(Box.createVerticalGlue());
        diagram = new InteractorDiagram(interactor);
        cellContent.add(diagram);
        cellContent.add(Box.createVerticalGlue());
        return cellContent;
    }

    private JComponent createSpecies(Interactor interactor) {
        JComponent speciesPanel = new VerticalPanel();
        speciesPanel.add(Box.createVerticalGlue());
        Matcher speciesMatcher = speciesPattern.matcher(interactor.species);
        if (speciesMatcher.find()) {
            speciesPanel.add(new CenteredLabel(speciesMatcher.group()));
            speciesPanel.add(new CenteredLabel(interactor.species.substring(speciesMatcher.end())));
        } else {
            speciesPanel.add(new CenteredLabel(interactor.species));
        }
        speciesPanel.add(Box.createVerticalGlue());
        speciesPanel.setAlignmentY(CENTER_ALIGNMENT);
        return speciesPanel;
    }


    private Cell addCell(JComponent cellContent, TermColumn column) {
        EasyGBC helper = column.isFixedInRowHeader ? table.resolver.rowHeaderHelper.noExpand() : layoutHelper;
        JPanel container = column.isFixedInRowHeader ? table.resolver.rowHeaderPanel : this;

        helper.gridx = column.ordinal();
        Cell cell = new Cell(cellContent);
        cell.setBackground(selected ? SELECTED_COLOR : UNSELECTED_COLOR);
        if (column != SELECT) cellContent.addMouseListener(mouseAdapter);
        cells.put(column, cell);

        container.add(cell, helper.expandBoth());

        int width = cellContent.getPreferredSize().width;
        if (table.resolver.maxWidthsOfColumns.get(column) < width) {
            table.resolver.maxWidthsOfColumns.put(column, width);
        }
        return cell;
    }

    public void updatePreview() {
        diagram.updateStyle();
    }

    public void homogenizeWidth() {
        for (Map.Entry<TermColumn, Integer> entry : table.resolver.maxWidthsOfColumns.entrySet()) {
            if (entry.getValue() != 0 && cells.containsKey(entry.getKey())) {
                Cell cell = cells.get(entry.getKey());
                Dimension size = cell.getPreferredSize();
                size.width = entry.getValue() + 10;
                cell.setSize(size);
                cell.setPreferredSize(size);
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) selected = true;
        else if (e.getStateChange() == ItemEvent.DESELECTED) selected = false;

        cells.values().forEach(cell -> cell.setBackground(selected ? SELECTED_COLOR : UNSELECTED_COLOR));
        repaint();
    }

    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            selected = !selected;
            selectionCheckBox.setSelected(selected);
        }
    };

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        cells.values().forEach(cell -> cell.setVisible(aFlag));
    }
}
