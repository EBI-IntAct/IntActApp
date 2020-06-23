package uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution;

import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.ui.components.IButton;
import uk.ac.ebi.intact.app.internal.ui.components.panels.FloatingPanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LimitExceededPanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution.TermColumn.TERM;

class TermTable extends JPanel implements ItemListener {
    public static final Color TERM_BG = new Color(161, 135, 184);
    private final Boolean includeAllInteractorsOption;
    final ResolveTermsPanel resolver;
    final String term;
    final List<Interactor> interactors;
    final boolean isPaged;
    final Map<Interactor, Row> rows = new HashMap<>();
    final EasyGBC layoutHelper = new EasyGBC();
    boolean includeAll = false;

    private IButton selectAll;
    private IButton unselectAll;
    private JPanel termControlPanel;

    public TermTable(ResolveTermsPanel resolver, String term, List<Interactor> interactors, boolean isPaged) {
        this.resolver = resolver;
        this.term = term;
        this.interactors = interactors;
        this.isPaged = isPaged;
        includeAllInteractorsOption = resolver.manager.option.DEFAULT_INCLUDE_ALL_INTERACTORS.getValue();
        init();
    }

    private void init() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        addTermTitle();
        if (interactors == null || interactors.isEmpty()) {
            add(new JLabel("Not found"), layoutHelper.right().expandHoriz());
        } else {

            for (Interactor interactor : interactors) {
                Row row = new Row(interactor, this);
                row.silenceSelection(isPaged && includeAllInteractorsOption);
                resolver.rowHeaderHelper.down();
                add(row, layoutHelper.down().expandBoth());
                rows.put(interactor, row);
            }
            if (isPaged) {
                LimitExceededPanel limitExceededPanel = new LimitExceededPanel("interactors", "matched", resolver.manager.option.MAX_INTERACTOR_PER_TERM.getValue(), "requery with a bigger number of interactor per terms via the option panel", JLabel.LEFT);
                limitExceededPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
                add(limitExceededPanel, layoutHelper.down().expandHoriz());
                resolver.rowHeaderPanel.add(Box.createVerticalStrut(limitExceededPanel.getPreferredSize().height), resolver.rowHeaderHelper.down().expandHoriz());
            }
        }
    }

    private void addTermTitle() {
        resolver.rowHeaderHelper.expandBoth();
        resolver.rowHeaderHelper.gridheight = interactors.size() + (isPaged ? 2 : 1);
        resolver.rowHeaderHelper.gridx = 0;
        termControlPanel = createTermControlPanel();
        FloatingPanel floatingPanel = new FloatingPanel(termControlPanel);
        resolver.rowHeaderPanel.add(floatingPanel, resolver.rowHeaderHelper);
        int width = floatingPanel.getPreferredSize().width + 4;
        if (resolver.maxWidthsOfColumns.get(TERM) < width) {
            resolver.maxWidthsOfColumns.put(TERM, width);
        }
        floatingPanel.setBackground(TERM_BG);
        floatingPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        resolver.rowHeaderHelper.gridheight = 1;
    }

    private JPanel createTermControlPanel() {
        JPanel termControlPanel = new VerticalPanel();

        JLabel label = new JLabel(term);
        label.setFont(label.getFont().deriveFont(Font.BOLD).deriveFont(14f));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(CENTER_ALIGNMENT);
        termControlPanel.add(label);

        selectAll = new IButton("Select all");
        selectAll.setEnabled(!resolver.selectedByDefault);
        selectAll.addActionListener(e -> {
            if (includeAllInteractorsOption) selectRows(true);
        });
        selectAll.setAlignmentX(CENTER_ALIGNMENT);
        selectAll.setDisabledColor(Color.WHITE);
        termControlPanel.add(selectAll);

        unselectAll = new IButton("Unselect all");
        unselectAll.setEnabled(resolver.selectedByDefault);
        unselectAll.addActionListener(e -> {
            if (includeAllInteractorsOption) selectRows(false);
        });
        unselectAll.setAlignmentX(CENTER_ALIGNMENT);
        unselectAll.setDisabledColor(Color.WHITE);
        termControlPanel.add(unselectAll);

        if (isPaged) {
            JCheckBox includeAll = new JCheckBox("Include all interactors");
            includeAll.setForeground(Color.white);
            includeAll.setHorizontalAlignment(SwingConstants.CENTER);
            includeAll.setAlignmentX(CENTER_ALIGNMENT);
            includeAll.setSelected(includeAllInteractorsOption);
            includeAll.addActionListener(e -> {
                this.includeAll = includeAll.isSelected();
                if (this.includeAll) {
                    rows.values().forEach(row -> row.silenceSelection(true));
                } else {
                    rows.values().forEach(row -> row.silenceSelection(false));
                }
            });
            termControlPanel.add(includeAll);
        }

        return termControlPanel;
    }

    public void selectRows(boolean b) {
        rows.values().forEach(row -> {
            if (row.isVisible()) row.selectionCheckBox.setSelected(b);
        });
    }


    public void homogenizeWidth() {
        rows.values().forEach(Row::homogenizeWidth);
    }

    public void updatePreviews() {
        rows.values().forEach(Row::updatePreview);
    }

    public List<Interactor> getDeselectedInteractors() {
        return rows.values().stream()
                .filter(row -> !row.selected)
                .map(row -> row.interactor)
                .collect(Collectors.toList());
    }

    public List<Interactor> getSelectedInteractors() {
        return rows.values().stream()
                .filter(row -> row.selected && row.isVisible())
                .map(row -> row.interactor)
                .collect(Collectors.toList());
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        long visibleRowsCount = rows.values().stream().filter(Row::isVisible).count();
        long selectedAndVisibleRowsCount = rows.values().stream().filter(Row::isVisible).filter(row -> row.selected).count();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            selectedAndVisibleRowsCount++;
        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            selectedAndVisibleRowsCount--;
        }

        selectAll.setEnabled(selectedAndVisibleRowsCount < visibleRowsCount);
        unselectAll.setEnabled(selectedAndVisibleRowsCount > 0);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension preferredSize = super.getPreferredSize();
        preferredSize.height = Math.max(preferredSize.height, termControlPanel.getPreferredSize().height);
        return preferredSize;
    }


    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        preferredSize.height = Math.max(preferredSize.height, termControlPanel.getPreferredSize().height + 2 * ResolveTermsPanel.TERM_SPACE - 2);
        return preferredSize;
    }
}
