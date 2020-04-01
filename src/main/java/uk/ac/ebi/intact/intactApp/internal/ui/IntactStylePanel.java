package uk.ac.ebi.intact.intactApp.internal.ui;

import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.OLSMapper;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.TaxIdDictionary;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class IntactStylePanel extends AbstractIntactPanel {


    public final static Color transparentBackground = new Color(229, 229, 229);
    public final static Color titleColor = new Color(156, 156, 156);
    private EasyGBC layoutHelper = new EasyGBC();
    private Map<Long, ColorPicker> colorPickers = new HashMap<>();

    public IntactStylePanel(final IntactManager manager) {
        super(manager);
        setBackground(transparentBackground);
        init();
        revalidate();
        repaint();
    }


    private void init() {
        setLayout(new GridBagLayout());

        JButton resetStylesButton = new JButton("Reset styles");
        resetStylesButton.addActionListener(e -> {
            new Thread(() -> {
                manager.resetStyles();
                OLSMapper.originalKingdomColors.forEach((taxId, paint) -> colorPickers.get(taxId).setCurrentColor((Color) paint));
                OLSMapper.originalTaxIdToPaint.forEach((taxId, paint) -> colorPickers.get(taxId).setCurrentColor((Color) paint));
            }).start();
        });
        add(resetStylesButton);
        createLegend(OLSMapper.taxIdToPaint, false);
        add(new JSeparator(SwingConstants.HORIZONTAL), layoutHelper.down().anchor("west").expandHoriz());
        createLegend(OLSMapper.kingdomColors, true);

    }

    private void createLegend(Map<Long, Paint> reference, boolean italic) {

        EasyGBC d = new EasyGBC();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(transparentBackground);
//        Border border = BorderFactory.createLineBorder(titleColor);
//        TitledBorder titledBorder = BorderFactory.createTitledBorder(border, title, TitledBorder.CENTER, TitledBorder.TOP);
//        panel.setBorder(titledBorder);
        reference.forEach((taxId, paint) -> {
            ColorPicker colorPicker = new ColorPicker(TaxIdDictionary.getTaxIdDescriptor(taxId), (Color) paint, italic);
            colorPicker.addColorChangedListener(e -> {
                manager.updateStylesColorScheme(taxId, e.newColor);
                reference.put(taxId, e.newColor);
            });
            colorPickers.put(taxId, colorPicker);
            panel.add(colorPicker, d.anchor("west").down().expandHoriz());
        });
        add(panel, layoutHelper.down().anchor("west").expandHoriz());
    }

    @Override
    void doFilter(String type) {

    }


}

