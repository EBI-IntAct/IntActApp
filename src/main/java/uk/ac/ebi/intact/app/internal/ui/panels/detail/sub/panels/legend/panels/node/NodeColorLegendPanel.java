package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.node;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings.ColorSetting;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings.events.ColorSettingLoadedEvent;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings.events.ColorSettingLoadedListener;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions.Taxons;
import uk.ac.ebi.intact.app.internal.ui.components.buttons.IButton;
import uk.ac.ebi.intact.app.internal.ui.components.legend.NodeColorLegendEditor;
import uk.ac.ebi.intact.app.internal.ui.components.legend.NodeColorPicker;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.AbstractLegendPanel;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;
import uk.ac.ebi.intact.app.internal.utils.TimeUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class NodeColorLegendPanel extends AbstractLegendPanel implements ColorSettingLoadedListener {
    public final Map<String, NodeColorPicker> colorPickers = new HashMap<>();
    private static final ImageIcon add = IconUtils.createImageIcon("/Buttons/add.png");
    private final IButton addNodeColorButton = new IButton(add);
    private final JPanel addNodeColorPanel = new JPanel();
    private final JPanel userDefinedSpeciesPanel = new JPanel(new GridBagLayout());

    public NodeColorLegendPanel(Manager manager, Network currentNetwork, NetworkView currentView) {
        super("<html>Node Color <em>~ Species</em></html>", manager, currentNetwork, currentView);
        manager.utils.registerAllServices(this, new Properties());
        createNodeColorLegend(Taxons.getSpecies());
        addSeparator();
        createNodeColorLegend(List.of(Taxons.CHEMICAL_SYNTHESIS));
        createUserDefinedNodeColors();
        addSeparator();
        createNodeColorLegend(Taxons.getKingdoms());
        handleEvent(new ColorSettingLoadedEvent(manager.style.settings));
    }


    private void createNodeColorLegend(List<Taxons> taxons) {
        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(UIColors.lightBackground);

        taxons.forEach((taxon) -> {
            Map<String, Paint> reference = (taxon.isSpecies) ? StyleMapper.speciesColors : StyleMapper.kingdomColors;
            NodeColorPicker nodeColorPicker = new NodeColorPicker(manager, taxon.taxId, taxon.descriptor, (Color) reference.get(taxon.taxId), taxon.isSpecies);
            nodeColorPicker.addColorChangedListener(e -> manager.style.updateStylesColorScheme(taxon.taxId, e.newColor, true, !taxon.isSpecies));
            colorPickers.put(taxon.taxId, nodeColorPicker);
            panel.add(nodeColorPicker, layoutHelper.down().anchor("west").expandHoriz());
        });
        content.add(panel, layoutHelper.down().anchor("west").expandHoriz());
    }

    private void createUserDefinedNodeColors() {
        content.add(userDefinedSpeciesPanel, layoutHelper.down().expandHoriz());
        addNodeColorPanel.setBackground(UIColors.lightBackground);
        addNodeColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

        addNodeColorButton.addActionListener(e -> {
            userDefinedSpeciesPanel.add(new NodeColorLegendEditor(currentNetwork, addNodeColorPanel), layoutHelper.down().expandHoriz().anchor("west"));
            if (currentNetwork.getNonDefinedTaxon().isEmpty())
                addNodeColorPanel.setVisible(false);
            revalidate();
            repaint();
        });

        addNodeColorButton.setBackground(UIColors.lightBackground);
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
            Set<String> networkTaxIds = currentNetwork.getTaxIds();

            for (String taxId : colorPickers.keySet()) {
                colorPickers.get(taxId).setVisible(
                        networkTaxIds.contains(taxId) ||
                                (StyleMapper.taxIdToChildrenTaxIds.containsKey(taxId) && CollectionUtils.anyCommonElement(networkTaxIds, StyleMapper.taxIdToChildrenTaxIds.get(taxId)))
                );
            }
            addNodeColorPanel.setVisible(!currentNetwork.getNonDefinedTaxon().isEmpty());
        });
    }

    @Override
    public void handleEvent(ColorSettingLoadedEvent e) {
        Consumer<ColorSetting> colorSettingConsumer = setting -> {
            if (colorPickers.containsKey(setting.getTaxId()))
                colorPickers.get(setting.getTaxId()).setCurrentColor(setting.getColor());
        };
        e.getSource().getSpeciesSettings().forEach(colorSettingConsumer);
        e.getSource().getKingdomSettings().forEach(colorSettingConsumer);
        NodeColorLegendEditor.clearAll(false);
        e.getSource().getUserSettings().forEach(setting -> userDefinedSpeciesPanel.add(new NodeColorLegendEditor(manager, setting, addNodeColorPanel), layoutHelper.down().expandHoriz().anchor("west")));

        if (currentNetwork.getNonDefinedTaxon().isEmpty())
            addNodeColorPanel.setVisible(false);
    }
}
