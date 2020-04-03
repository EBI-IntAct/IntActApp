package uk.ac.ebi.intact.intactApp.internal.ui;

import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.OLSMapper;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.Taxon;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class IntactLegendPanel extends AbstractIntactPanel {
    public final static Color tabbedBackground = new Color(229, 229, 229);
    public final static Color transparent = new Color(229, 229, 229,0);
    private EasyGBC layoutHelper = new EasyGBC();
    private Map<Long, ColorPicker> colorPickers = new HashMap<>();
    private JPanel mainPanel = new JPanel(new GridBagLayout());
    private JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    public IntactLegendPanel(final IntactManager manager) {
        super(manager);
        setBackground(tabbedBackground);
        mainPanel.setBackground(Color.white);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        init();
        revalidate();
        repaint();
    }


    private void init() {
        setLayout(new GridBagLayout());
        createResetStyleButton();
        createLegend(Taxon.getSpecies(), false);
        addSeparator();
        createLegend(List.of(Taxon.CHEMICAL_SYNTHESIS), false);
        addSeparator();
        createLegend(Taxon.getKingdoms(), true);
        add(scrollPane, new EasyGBC().down().anchor("west").expandBoth());
    }

    private void createResetStyleButton() {
        JButton resetStylesButton = new JButton("Reset styles");
        resetStylesButton.addActionListener(e -> new Thread(() -> {
            manager.resetStyles();
            OLSMapper.originalKingdomColors.forEach((taxId, paint) -> colorPickers.get(taxId).setCurrentColor((Color) paint));
            OLSMapper.originalTaxIdToPaint.forEach((taxId, paint) -> colorPickers.get(taxId).setCurrentColor((Color) paint));
        }).start());
        mainPanel.add(resetStylesButton);
    }

    private void addSeparator() {
        JSeparator separator;
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.getPreferredSize().height = 1;
        mainPanel.add(separator, layoutHelper.down().anchor("west").expandHoriz());
    }

    private void createLegend(List<Taxon> taxons, boolean italic) {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(transparent);

        taxons.forEach((taxon) -> {
            Map<Long, Paint> reference = (taxon.isSpecies) ? OLSMapper.taxIdToPaint : OLSMapper.kingdomColors;
            ColorPicker colorPicker = new ColorPicker(taxon.descriptor, (Color) reference.get(taxon.taxId), italic);
            colorPicker.addColorChangedListener(e -> {
                manager.updateStylesColorScheme(taxon.taxId, e.newColor);
                reference.put(taxon.taxId, e.newColor);
            });
            colorPickers.put(taxon.taxId, colorPicker);
            panel.add(colorPicker, layoutHelper.down().anchor("west").expandHoriz());
        });
        mainPanel.add(panel, layoutHelper.down().anchor("west").expandHoriz());
    }

    @Override
    void doFilter(String type) {
    }


}

