package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.legend.panels.node;

import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.Taxons;
import uk.ac.ebi.intact.intactApp.internal.ui.components.legend.NodeColorLegendEditor;
import uk.ac.ebi.intact.intactApp.internal.ui.components.legend.NodeColorPicker;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.legend.panels.AbstractLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.CollectionUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TimeUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.*;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;

public class NodeColorLegendPanel extends AbstractLegendPanel {
    public final Map<Long, NodeColorPicker> colorPickers = new HashMap<>();
    private static final ImageIcon add = IconUtils.createImageIcon("/Buttons/add.png");
    private final JButton addNodeColorButton = new JButton(add);
    private final JPanel addNodeColorPanel = new JPanel();

    public NodeColorLegendPanel(IntactManager manager, IntactNetwork currentINetwork, IntactNetworkView currentIView) {
        super("<html>Node Color <em>~ Species</em></html>", manager, currentINetwork, currentIView);
        createNodeColorLegend(Taxons.getSpecies());
        addSeparator();
        createNodeColorLegend(List.of(Taxons.CHEMICAL_SYNTHESIS));
        createUserDefinedNodeColors();
        addSeparator();
        createNodeColorLegend(Taxons.getKingdoms());
    }


    private void createNodeColorLegend(List<Taxons> taxons) {
        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(backgroundColor);

        taxons.forEach((taxon) -> {
            Map<Long, Paint> reference = (taxon.isSpecies) ? StyleMapper.taxIdToPaint : StyleMapper.kingdomColors;
            NodeColorPicker nodeColorPicker = new NodeColorPicker(taxon.descriptor, (Color) reference.get(taxon.taxId), taxon.isSpecies);
            nodeColorPicker.addColorChangedListener(e -> {
                manager.style.updateStylesColorScheme(taxon.taxId, e.newColor, true);
                reference.put(taxon.taxId, e.newColor);
            });
            colorPickers.put(taxon.taxId, nodeColorPicker);
            panel.add(nodeColorPicker, layoutHelper.down().anchor("west").expandHoriz());
        });
        content.add(panel, layoutHelper.down().anchor("west").expandHoriz());
    }

    private void createUserDefinedNodeColors() {
        JPanel userDefinedSpeciesPanel = new JPanel(new GridBagLayout());
        content.add(userDefinedSpeciesPanel, layoutHelper.down().expandHoriz());
        addNodeColorPanel.setBackground(backgroundColor);
        addNodeColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT,4,2));

        addNodeColorButton.addActionListener(e -> {
            userDefinedSpeciesPanel.add(new NodeColorLegendEditor(currentINetwork, addNodeColorPanel), layoutHelper.down().expandHoriz().anchor("west"));
            if (currentINetwork.getNonDefinedTaxon().isEmpty())
                addNodeColorPanel.setVisible(false);
            revalidate();
            repaint();
        });

        addNodeColorButton.setBackground(backgroundColor);
        addNodeColorButton.setOpaque(true);
        addNodeColorButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        addNodeColorPanel.add(addNodeColorButton);

        JLabel label = new JLabel("Add new node color legend");
        label.setBorder(new EmptyBorder(0, 4, 0, 0));
        addNodeColorPanel.add(label, layoutHelper);
        addNodeColorPanel.setVisible(false);
        new Thread(() -> {
            while (StyleMapper.speciesNotReady())
                TimeUtils.sleep(200);
            addNodeColorPanel.setVisible(true);
        }).start();

        content.add(addNodeColorPanel, layoutHelper.anchor("west").down().noExpand());
    }

    @Override
    public void filterCurrentLegend() {
        executor.execute(() -> {
            Set<Long> networkTaxIds = currentINetwork.getTaxIds();

            for (Long taxId : colorPickers.keySet()) {
                colorPickers.get(taxId).setVisible(
                        networkTaxIds.contains(taxId) ||
                                (StyleMapper.taxIdToChildrenTaxIds.containsKey(taxId) && CollectionUtils.anyCommonElement(networkTaxIds, StyleMapper.taxIdToChildrenTaxIds.get(taxId)))
                );
            }
            addNodeColorPanel.setVisible(!currentINetwork.getNonDefinedTaxon().isEmpty());
        });
    }
}
