package uk.ac.ebi.intact.app.internal.ui.components.legend;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.ui.components.combo.box.models.SortedComboBoxModel;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class NodeColorLegendEditor extends NodeColorPicker implements NodeColorPicker.ColorChangedListener {
    private static final ImageIcon remove = IconUtils.createImageIcon("/Buttons/remove.png");
    private static final List<NodeColorLegendEditor> NODE_COLOR_LEGEND_EDITOR_LIST = new ArrayList<>();

    protected Network currentNetwork;
    protected JComponent addNewNodeLegendEditorActivator;
    protected String currentTaxId;
    protected Manager manager;
    protected JComboBox<String> speciesField;
    protected JButton removeButton = new JButton(remove);

    public NodeColorLegendEditor(Network currentNetwork, JComponent addNewNodeLegendEditorActivator) {
        this.currentNetwork = currentNetwork;
        this.manager = currentNetwork.manager;
        this.addNewNodeLegendEditorActivator = addNewNodeLegendEditorActivator;
        NODE_COLOR_LEGEND_EDITOR_LIST.add(this);

        setSpeciesField(getSpeciesOptions());
        descriptor = (String) speciesField.getSelectedItem();
        currentTaxId = currentNetwork.getSpeciesId(descriptor);
        currentColor = (Color) StyleMapper.kingdomColors.get(currentTaxId);
        updateStyleColors();
        definedSpecies = true;
        editableBall = new EditableBall(currentColor, 30);

        setRemoveButton();

        add(editableBall);
        add(speciesField);
        add(removeButton);
        addColorChangedListener(this);
    }

    private Vector<String> getSpeciesOptions() {
        return new Vector<>(currentNetwork.getNonDefinedTaxon());
    }

    private void setSpeciesField(Vector<String> speciesOptions) {
        speciesField = new JComboBox<>(new SortedComboBoxModel<>(speciesOptions));
        for (NodeColorLegendEditor nodeColorLegendEditor : NODE_COLOR_LEGEND_EDITOR_LIST) {
            if (nodeColorLegendEditor != this && nodeColorLegendEditor.speciesField != null) {
                nodeColorLegendEditor.speciesField.removeItem(speciesField.getSelectedItem());
            }
        }

        speciesField.setFont(italicFont);
        speciesField.setBorder(new EmptyBorder(0, 4, 0, 0));
        speciesField.setPrototypeDisplayValue((String) speciesField.getSelectedItem());
        speciesField.addItemListener(e -> {
            switch (e.getStateChange()) {
                case ItemEvent.DESELECTED:
                    resetFormerLegend();

                    String unselectedSpecies = (String) e.getItem();
                    for (NodeColorLegendEditor nodeColorLegendEditor : NODE_COLOR_LEGEND_EDITOR_LIST) {
                        if (nodeColorLegendEditor != this) {
                            nodeColorLegendEditor.speciesField.addItem(unselectedSpecies);
                        }
                    }
                    break;
                case ItemEvent.SELECTED:
                    descriptor = (String) e.getItem();
                    currentTaxId = currentNetwork.getSpeciesId(descriptor);
                    currentColor = (Color) StyleMapper.kingdomColors.get(currentTaxId);
                    editableBall.setColor(currentColor);
                    speciesField.setPrototypeDisplayValue(descriptor);

                    for (NodeColorLegendEditor nodeColorLegendEditor : NODE_COLOR_LEGEND_EDITOR_LIST) {
                        if (nodeColorLegendEditor != this) {
                            nodeColorLegendEditor.speciesField.removeItem(descriptor);
                        }
                    }
                    break;
            }
        });

    }

    private void resetFormerLegend() {
        String kingdom = StyleMapper.taxIdToParentTaxId.get(currentTaxId);
        Color formerColor = (Color) StyleMapper.kingdomColors.get(kingdom);
        if (formerColor != null)
            manager.style.updateStylesColorScheme(currentTaxId, formerColor, false);
        StyleMapper.taxIdToPaint.remove(currentTaxId);
    }

    private void setRemoveButton() {
        removeButton.addActionListener(e -> this.destroy(true));
        removeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        removeButton.setSize(30, 30);
    }

    private void updateStyleColors() {
        manager.style.updateStylesColorScheme(currentTaxId, currentColor, false);
    }

    public void destroy(boolean resetFormerLegend) {
        if (resetFormerLegend) resetFormerLegend();

        addNewNodeLegendEditorActivator.setVisible(true);

        Container container = getParent();
        container.remove(this);
        container.getParent().getParent().revalidate();
        container.getParent().getParent().repaint();

        NODE_COLOR_LEGEND_EDITOR_LIST.remove(this);
        for (NodeColorLegendEditor nodeColorLegendEditor : NODE_COLOR_LEGEND_EDITOR_LIST) {
            nodeColorLegendEditor.speciesField.addItem(descriptor);
        }
    }

    public void networkChanged(Network newNetwork) {
        currentNetwork = newNetwork;
    }


    @Override
    public void colorChanged(ColorChangedEvent colorChangedEvent) {
        currentColor = colorChangedEvent.newColor;
        updateStyleColors();
    }

    public static List<NodeColorLegendEditor> getNodeColorLegendEditorList() {
        return new ArrayList<>(NODE_COLOR_LEGEND_EDITOR_LIST);
    }

    public static void clearAll(boolean resetFormerLegend) {
        new ArrayList<>(NODE_COLOR_LEGEND_EDITOR_LIST).forEach(editor -> editor.destroy(resetFormerLegend));
    }
}
