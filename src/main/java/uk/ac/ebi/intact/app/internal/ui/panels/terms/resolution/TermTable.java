package uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution;

import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.ui.components.buttons.IButton;
import uk.ac.ebi.intact.app.internal.ui.components.panels.FloatingPanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution.TermColumn.TERM;

class TermTable extends JPanel implements ItemListener {
    public static final Color TERM_BG = new Color(161, 135, 184);
    final WeakReference<ResolveTermsPanel> resolver;
    final String term;
    final boolean isPaged;
    final int totalInteractors;
    final Map<Interactor, Row> rows = new HashMap<>();
    final EasyGBC layoutHelper = new EasyGBC();
    boolean includeAdditionalInteractors = false;

    private LimitRow limitRow;
    private IButton selectAll;
    private IButton unselectAll;
    private JPanel termControlPanel;

    public TermTable(ResolveTermsPanel resolver, String term, List<Interactor> interactors, int totalInteractors) {
        this.resolver = new WeakReference<>(resolver);
        this.term = term;
        this.totalInteractors = totalInteractors;
        this.isPaged = totalInteractors > interactors.size();
        init(interactors);
    }

    private void init(List<Interactor> interactors) {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        addTermTitle(interactors.size());
        if (interactors.isEmpty()) {
            add(new JLabel("Not found"), layoutHelper.right().expandHoriz());
        } else {
            for (Interactor interactor : interactors) {
                Row row = new Row(interactor, this);
                getResolver().rowHeaderHelper.down();
                add(row, layoutHelper.down().expandBoth());
                rows.put(interactor, row);
            }
            if (isPaged) {
                getResolver().rowHeaderHelper.down();
                limitRow = new LimitRow(this);
                add(limitRow, layoutHelper.down().expandBoth());
            }
        }
    }

    private void addTermTitle(int nbInteractors) {
        getResolver().rowHeaderHelper.expandBoth();
        getResolver().rowHeaderHelper.gridheight = nbInteractors + (isPaged ? 2 : 1);
        getResolver().rowHeaderHelper.gridx = 0;
        termControlPanel = createTermControlPanel();
        FloatingPanel floatingPanel = new FloatingPanel(termControlPanel);
        getResolver().rowHeaderPanel.add(floatingPanel, getResolver().rowHeaderHelper);
        int width = floatingPanel.getPreferredSize().width + 4;
        if (getResolver().maxWidthsOfColumns.get(TERM) < width) {
            getResolver().maxWidthsOfColumns.put(TERM, width);
        }
        floatingPanel.setBackground(TERM_BG);
        floatingPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
        getResolver().rowHeaderHelper.gridheight = 1;
    }

    private JPanel createTermControlPanel() {
        JPanel termControlPanel = new VerticalPanel();

        JLabel label = new JLabel(term);
        label.setFont(label.getFont().deriveFont(Font.BOLD).deriveFont(14f));
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(CENTER_ALIGNMENT);
        termControlPanel.add(label);

        selectAll = new IButton("<div style=\"text-align: center;\">Select all<br>previewed<br>interactors</div>");
        selectAll.setEnabled(!getResolver().selectedByDefault);
        selectAll.addActionListener(e -> selectRows(true));
        selectAll.setAlignmentX(CENTER_ALIGNMENT);
        selectAll.setDisabledColor(Color.WHITE);
        termControlPanel.add(selectAll);

        unselectAll = new IButton("<div style=\"text-align: center;\">Unselect all<br>previewed<br>interactors</div>");
        unselectAll.setEnabled(getResolver().selectedByDefault);
        unselectAll.addActionListener(e -> selectRows(false));
        unselectAll.setAlignmentX(CENTER_ALIGNMENT);
        unselectAll.setDisabledColor(Color.WHITE);
        termControlPanel.add(unselectAll);
        return termControlPanel;
    }

    public void selectRows(boolean b) {
        rows.values().forEach(row -> {
            if (row.isVisible()) row.selectionCheckBox.setSelected(b);
        });
    }


    public void homogenizeWidth() {
        rows.values().forEach(Row::homogenizeWidth);
        if (limitRow != null) limitRow.homogenizeWidth();
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
    
    public ResolveTermsPanel getResolver() {
        return requireNonNull(resolver.get());
    }
}
