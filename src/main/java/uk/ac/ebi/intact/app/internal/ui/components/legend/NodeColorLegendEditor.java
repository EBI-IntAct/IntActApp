package uk.ac.ebi.intact.app.internal.ui.components.legend;

import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.*;

public class NodeColorLegendEditor extends NodeColorPicker implements NodeColorPicker.ColorChangedListener {
    private static final ImageIcon remove = IconUtils.createImageIcon("/Buttons/remove.png");
    private static final Map<String, Color> originalColors = new HashMap<>();
    private static final List<NodeColorLegendEditor> NODE_COLOR_LEGEND_EDITOR_LIST = new ArrayList<>();

    protected Network currentINetwork;
    protected JComponent addNewNodeLegendEditorActivator;
    protected long currentTaxId;
    protected Manager manager;
    protected JComboBox<String> speciesField;
//    protected JCheckBox includeSubSpecies = new JCheckBox("Include subtaxons");
    protected JButton removeButton = new JButton(remove);

    public NodeColorLegendEditor(Network currentINetwork, JComponent addNewNodeLegendEditorActivator) {
        this.currentINetwork = currentINetwork;
        this.manager = currentINetwork.getManager();
        this.addNewNodeLegendEditorActivator = addNewNodeLegendEditorActivator;
        NODE_COLOR_LEGEND_EDITOR_LIST.add(this);

        Vector<String> speciesOptions = getSpeciesOptions();
        String firstItem = speciesOptions.firstElement();
        descriptor = firstItem;
        currentTaxId = currentINetwork.getSpeciesId(descriptor);
        currentColor = (Color) StyleMapper.kingdomColors.get(currentTaxId);
        originalColors.put(firstItem, currentColor);
        updateStyleColors();
        definedSpecies = true;
        editableBall = new EditableBall(currentColor, 30);

        setSpeciesField(speciesOptions);

//        setIncludeSubSpeciesCheckBox();

        setRemoveButton();

        add(editableBall);
        add(speciesField);
//        add(includeSubSpecies);
        add(removeButton);
        addColorChangedListener(this);
    }

    private Vector<String> getSpeciesOptions() {
        return new Vector<>(currentINetwork.getNonDefinedTaxon());
    }

    private void setSpeciesField(Vector<String> speciesOptions) {
        speciesField = new JComboBox<>(speciesOptions);
        for (NodeColorLegendEditor nodeColorLegendEditor : NODE_COLOR_LEGEND_EDITOR_LIST) {
            if (nodeColorLegendEditor != this) {
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
                    currentTaxId = currentINetwork.getSpeciesId(descriptor);
                    speciesField.setPrototypeDisplayValue(descriptor);
                    originalColors.put(descriptor, (Color) StyleMapper.kingdomColors.get(currentTaxId));

                    for (NodeColorLegendEditor nodeColorLegendEditor : NODE_COLOR_LEGEND_EDITOR_LIST) {
                        if (nodeColorLegendEditor != this) {
                            nodeColorLegendEditor.speciesField.removeItem(descriptor);
                        }
                    }

                    // set selected color
                    updateStyleColors();
                    break;
            }
        });

    }

    private void resetFormerLegend() {
        Color formerColor = originalColors.remove(descriptor);
        if (formerColor != null)
            manager.style.updateStylesColorScheme(currentTaxId, formerColor, false);
        StyleMapper.taxIdToPaint.remove(currentTaxId);
    }

//    private void setIncludeSubSpeciesCheckBox() {
//        includeSubSpecies.addActionListener(e -> {
//            updateStyleColors();
//            if (!includeSubSpecies.isSelected()) {
//                for (Long children : OLSMapper.taxIdToChildrenTaxIds.get(currentTaxId)) {
//                    manager.styleManager.updateStylesColorScheme(children, originalColors.get(descriptor), false);
//                }
//            }
//        });
//    }

    private void setRemoveButton() {
        removeButton.addActionListener(e -> this.destroy());
        removeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        removeButton.setSize(30, 30);
    }

    private void updateStyleColors() {
        manager.style.updateStylesColorScheme(currentTaxId, currentColor, false);
    }

    public void destroy() {
        resetFormerLegend();

        addNewNodeLegendEditorActivator.setVisible(true);

        Container container = getParent();
        container.remove(this);
        container.getParent().getParent().revalidate();
        container.getParent().getParent().repaint();

        for (NodeColorLegendEditor nodeColorLegendEditor : NODE_COLOR_LEGEND_EDITOR_LIST) {
            nodeColorLegendEditor.speciesField.addItem(descriptor);
        }
    }

    public void networkChanged(Network newINetwork) {
        currentINetwork = newINetwork;
    }


    @Override
    public void colorChanged(ColorChangedEvent colorChangedEvent) {
        currentColor = colorChangedEvent.newColor;
        updateStyleColors();
    }

    public static List<NodeColorLegendEditor> getNodeColorLegendEditorList() {
        return new ArrayList<>(NODE_COLOR_LEGEND_EDITOR_LIST);
    }
}
