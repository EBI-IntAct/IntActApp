package uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution;

import org.cytoscape.work.TaskFactory;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.tasks.query.factories.ImportNetworkTaskFactory;
import uk.ac.ebi.intact.app.internal.ui.components.filler.HorizontalFiller;
import uk.ac.ebi.intact.app.internal.ui.components.labels.CenteredLabel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.panels.options.OptionsPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.ComponentUtils;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import static uk.ac.ebi.intact.app.internal.ui.utils.ComponentUtils.SizeType;
import static uk.ac.ebi.intact.app.internal.ui.utils.ComponentUtils.resizeHeight;

public class ResolveTermsPanel extends JPanel implements ItemListener {
    public static final int HEIGHT = 400;
    public static final int TERM_SPACE = 8;
    private static final ImageIcon filterIcon = IconUtils.createImageIcon("/IntAct/DIGITAL/filter.png");
    public static final Color HEADER_CELLS_COLOR = new Color(104, 41, 124);
    final Manager manager;
    final EasyGBC layoutHelper = new EasyGBC();
    final boolean includeNonAmbiguousTerms;
    final boolean selectedByDefault;

    Network network;

    final JPanel displayPanel = new JPanel(new GridBagLayout());
    final Map<TermColumn, Cell> columns = new HashMap<>();
    final Map<TermColumn, Integer> maxWidthsOfColumns = Arrays.stream(TermColumn.values())
            .collect(toMap(termColumn -> termColumn, termColumn -> 0));

    private final Map<TermColumn, Set<Object>> visibleColumnValues = Arrays.stream(TermColumn.values())
            .filter(column -> column.filtered)
            .collect(toMap(column -> column, column -> new HashSet<>()));

    final Map<TermColumn, FilterMenu> columnFilterMenus = Arrays.stream(TermColumn.values())
            .filter(column -> column.filtered)
            .collect(toMap(column -> column, FilterMenu::new));

    final List<TermTable> termTables = new ArrayList<>();

    final EasyGBC columnHeaderHelper = new EasyGBC();
    final JPanel columnHeaderPanel = new JPanel(new GridBagLayout());

    final EasyGBC rowHeaderHelper = new EasyGBC();
    final JPanel rowHeaderPanel = new JPanel(new GridBagLayout());
    private final JPanel rowHeaderContainerPanel = new JPanel(new GridBagLayout());

    final EasyGBC tableCornerHelper = new EasyGBC();
    final JPanel tableCornerPanel = new JPanel(new GridBagLayout());
    private JButton selectAllButton;
    private JButton unSelectAllButton;
    private final Set<Interactor> interactorsToQuery = new HashSet<>();

    public ResolveTermsPanel(final Manager manager, Network network) {
        this(manager, network, true, true);
    }

    public ResolveTermsPanel(final Manager manager, Network network, boolean selectedByDefault, boolean includeNonAmbiguousTerms) {
        super(new GridBagLayout());
        this.manager = manager;
        this.network = network;
        this.selectedByDefault = selectedByDefault;
        this.includeNonAmbiguousTerms = includeNonAmbiguousTerms;
        init();
    }

    private void init() {
        if (includeNonAmbiguousTerms) {
            add(new CenteredLabel("The terms you have given matches all these interactors.", 15, HEADER_CELLS_COLOR), layoutHelper.expandHoriz());
            add(new CenteredLabel("Select interactors you want to use as seeds to build the cyNetwork around", 14, HEADER_CELLS_COLOR), layoutHelper.down().expandHoriz());
        } else {
            add(new CenteredLabel("There is ambiguity among the terms you gave.", 15, HEADER_CELLS_COLOR), layoutHelper.down().expandHoriz());
            add(new CenteredLabel("Please select the interactors that you meant to query as seeds to build the cyNetwork around.", 14, HEADER_CELLS_COLOR), layoutHelper.down().expandHoriz());
        }
        createColumnHeader();
        createRowHeader();
        initScrollPanel();
        fillDisplayPanel();
        createFilters();
        createOptionPanel();
        createControlButtons();
        add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
    }

    private void createRowHeader() {
        rowHeaderContainerPanel.setBackground(Color.WHITE);
        EasyGBC c = new EasyGBC();
        rowHeaderContainerPanel.add(rowHeaderPanel, c.expandHoriz().anchor("northwest"));
        rowHeaderContainerPanel.add(Box.createVerticalGlue(), c.expandBoth());
    }

    private void createColumnHeader() {
        columnHeaderPanel.setBackground(Color.WHITE);
        rowHeaderPanel.setBackground(Color.WHITE);
        columnHeaderHelper.anchor("west");
        Arrays.stream(TermColumn.values()).forEach(this::addHeaderCell);
        layoutHelper.down();
    }

    private void addHeaderCell(TermColumn column) {
        EasyGBC helper = column.isFixedInRowHeader ? tableCornerHelper : columnHeaderHelper;
        JPanel container = column.isFixedInRowHeader ? tableCornerPanel : columnHeaderPanel;
        helper.gridx = column.ordinal();

        JPanel content = new JPanel(new BorderLayout());
        JLabel label = new JLabel(column.name);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        content.add(label, BorderLayout.CENTER);
        if (column.filtered) {
            JLabel icon = new JLabel(filterIcon);
            icon.setBorder(new EmptyBorder(0, 0, 0, 15));
            content.add(icon, BorderLayout.EAST);
        }

        Cell cell = new Cell(content);
        if (container != tableCornerPanel) helper.expandHoriz();
        container.add(cell, helper);

        if (columnFilterMenus.containsKey(column)) {
            cell.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    FilterMenu menu = columnFilterMenus.get(column);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            });
        }

        int width = label.getPreferredSize().width;
        if (maxWidthsOfColumns.get(column) < width) {
            maxWidthsOfColumns.put(column, width);
        }

        cell.setBackground(HEADER_CELLS_COLOR);
        resizeHeight(cell, 30, SizeType.ALL);
        columns.put(column, cell);
    }


    private void fillDisplayPanel() {
        EasyGBC c = new EasyGBC();
        c.anchor("west");
        displayPanel.setBackground(Color.WHITE);
        network.getInteractorsToResolve().forEach((term, interactors) -> {
            if (includeNonAmbiguousTerms || interactors.size() > 1) {
                TermTable termTable = new TermTable(this, term, interactors, network.getPagedTerms().get(term));
                termTables.add(termTable);
                displayPanel.add(termTable, c.down().expandHoriz());
                displayPanel.add(Box.createVerticalStrut(TERM_SPACE), c.down().expandHoriz());
                rowHeaderPanel.add(Box.createVerticalStrut(TERM_SPACE), rowHeaderHelper.down().expandHoriz());
                rowHeaderHelper.down();
            } else {
                interactorsToQuery.addAll(interactors);
            }
        });
        if (!termTables.isEmpty()) {
            displayPanel.remove(displayPanel.getComponentCount() - 1);
            rowHeaderPanel.remove(rowHeaderPanel.getComponentCount() - 1);
            displayPanel.add(Box.createVerticalGlue(), c.down().expandBoth());
            homogenizeWidths();
        }
    }

    private void homogenizeWidths() {
        for (Map.Entry<TermColumn, Integer> entry : maxWidthsOfColumns.entrySet()) {
            int width = entry.getValue();
            TermColumn column = entry.getKey();
            if (width != 0 && columns.containsKey(column)) {
                if (column != TermColumn.TERM) width += 10;
                ComponentUtils.resizeWidth(columns.get(column), width, SizeType.PREF);
            }
        }
        termTables.forEach(TermTable::homogenizeWidth);
    }

    private void initScrollPanel() {
        JScrollPane scrollPane = new JScrollPane(displayPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAutoscrolls(true);

        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, tableCornerPanel);

        HorizontalFiller corner = new HorizontalFiller();
        corner.setBorder(BorderFactory.createMatteBorder(2, 1, 0, 1, Color.WHITE));
        scrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, corner);

        {
            JViewport viewport = new JViewport();
            viewport.setView(columnHeaderPanel);
            scrollPane.setColumnHeader(viewport);
        }
        {
            JViewport viewport = new JViewport();
            viewport.setView(rowHeaderContainerPanel);
            scrollPane.setRowHeader(viewport);
        }

        displayPanel.setMinimumSize(new Dimension(900, HEIGHT));
        displayPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        scrollPane.setMinimumSize(new Dimension(900, HEIGHT));
        scrollPane.setPreferredSize(new Dimension(900, HEIGHT));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        add(scrollPane, layoutHelper.down().expandHoriz());
    }


    private static class FilterMenu extends JPopupMenu {

        public final TermColumn column;

        public FilterMenu(TermColumn column) {
            super();
            this.column = column;
            add(new JLabel("Select values to show:"));
        }
    }

    private void createFilters() {
        for (TermTable table : termTables) {
            for (Interactor interactor : table.interactors) {
                visibleColumnValues.forEach((column, visibleValues) -> {
                    Object value = column.getValue.apply(interactor);
                    if (!visibleValues.contains(value)) {
                        visibleValues.add(value);
                        JCheckBox checkBox = new JCheckBox(value != null ? value.toString() : "", true);
                        columnFilterMenus.get(column).add(checkBox);
                        checkBox.addItemListener(e -> {
                            switch (e.getStateChange()) {
                                case ItemEvent.SELECTED:
                                    visibleValues.add(value);
                                    break;
                                case ItemEvent.DESELECTED:
                                    visibleValues.remove(value);
                                    break;
                            }
                            filterInteractors();
                        });
                    }
                });
            }
        }
    }

    private void createOptionPanel() {
        OptionsPanel optionsPanel = new OptionsPanel(manager, OptionManager.Scope.DISAMBIGUATION);
        optionsPanel.addListener(manager.option.SHOW_HIGHLIGHTS, () -> {
            boolean showHighlightsValue = !manager.option.SHOW_HIGHLIGHTS.getValue();
            termTables.forEach(table -> table.rows.values().forEach(row -> row.highlightMatchingColumns(showHighlightsValue)));
        });
        add(new CollapsablePanel("Options", optionsPanel, false), layoutHelper.down().expandHoriz());
    }

    private void filterInteractors() {
        for (TermTable table : termTables) {
            table.rows.forEach((interactor, row) -> row.setVisible(isToKeep(interactor)));
        }
    }

    private boolean isToKeep(Interactor interactor) {
        for (Map.Entry<TermColumn, Set<Object>> entry : visibleColumnValues.entrySet()) {
            Object value = entry.getKey().getValue.apply(interactor);
            Set<Object> visibleValues = entry.getValue();
            if (!visibleValues.contains(value)) {
                return false;
            }
        }
        return true;
    }

    private void createControlButtons() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 4));

        selectAllButton = new JButton("Select all");
        selectAllButton.addActionListener(e -> termTables.forEach(table -> table.selectRows(true)));
        selectAllButton.setEnabled(!selectedByDefault);
        controlPanel.add(selectAllButton);

        unSelectAllButton = new JButton("Unselect all");
        unSelectAllButton.addActionListener(e -> termTables.forEach(table -> table.selectRows(false)));
        unSelectAllButton.setEnabled(selectedByDefault);
        controlPanel.add(unSelectAllButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> close());
        controlPanel.add(cancelButton);

        JButton importButton = new JButton("Build Network");
        importButton.addActionListener(e -> {
            Map<String, List<Interactor>> missingInteractors = network.completeMissingInteractors(
                    termTables.stream()
                            .filter(termTable -> termTable.includeAll)
                            .map(termTable -> termTable.term)
                            .collect(toList()),
                    includeNonAmbiguousTerms
            );
            missingInteractors.values().forEach(interactorsToQuery::addAll);
            termTables.stream()
                    .map(TermTable::getSelectedInteractors)
                    .flatMap(Collection::stream).forEach(interactorsToQuery::add);
            if (interactorsToQuery.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No interactors selected. Please select at least one interactor.");
            } else {
                TaskFactory factory = new ImportNetworkTaskFactory(network, interactorsToQuery.stream().map(interactor -> interactor.ac).collect(toList()), manager.option.ADD_INTERACTING_PARTNERS.getValue(), null);
                manager.utils.execute(factory.createTaskIterator());
                close();
            }
        });
        controlPanel.add(importButton);

        add(controlPanel, layoutHelper.down().expandHoriz());
    }

    private void close() {
        ((Window) getRootPane().getParent()).dispose();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        boolean allSelected = e.getStateChange() == ItemEvent.SELECTED;
        for (TermTable table : termTables) {
            for (Row row : table.rows.values()) {
                if (row.isVisible() && row.selectionCheckBox != e.getSource()) {
                    if (allSelected != row.selected) {
                        selectAllButton.setEnabled(true);
                        unSelectAllButton.setEnabled(true);
                        return;
                    }
                }
            }
        }
        selectAllButton.setEnabled(!allSelected);
        unSelectAllButton.setEnabled(allSelected);
    }
}
