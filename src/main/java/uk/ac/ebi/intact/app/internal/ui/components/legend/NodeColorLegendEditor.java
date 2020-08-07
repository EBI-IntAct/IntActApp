package uk.ac.ebi.intact.app.internal.ui.components.legend;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings.ColorSetting;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.ui.components.combo.box.models.SortedComboBoxModel;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

public class NodeColorLegendEditor extends NodeColorPicker implements NodeColorPicker.ColorChangedListener {
    private static final ImageIcon remove = IconUtils.createImageIcon("/Buttons/remove.png");
    private static final List<NodeColorLegendEditor> NODE_COLOR_LEGEND_EDITOR_LIST = new ArrayList<>();
    private final ColorSetting setting;

    protected Network currentNetwork;
    protected JComponent addNewNodeLegendEditorActivator;
    protected String currentTaxId;
    protected JComboBox<String> speciesField;
    protected JButton removeButton = new JButton(remove);
    private SortedComboBoxModel<String> speciesModel;

    public NodeColorLegendEditor(Manager manager, ColorSetting setting, JComponent addNewNodeLegendEditorActivator) {
        super(manager);
        this.addNewNodeLegendEditorActivator = addNewNodeLegendEditorActivator;
        this.setting = setting;
        NODE_COLOR_LEGEND_EDITOR_LIST.add(this);

        descriptor = setting.getTaxonName();
        setSpeciesField(getSpeciesOptions(), descriptor);
        currentTaxId = setting.getTaxId();
        currentColor = setting.getColor();
        updateStyleColors();
        definedSpecies = true;
        editableBall = new EditableBall(currentColor, 30);

        setRemoveButton();

        add(editableBall);
        add(speciesField);
        add(removeButton);
        addColorChangedListener(this);
    }

    public NodeColorLegendEditor(Network currentNetwork, JComponent addNewNodeLegendEditorActivator) {
        super(currentNetwork.manager);
        this.currentNetwork = currentNetwork;
        this.addNewNodeLegendEditorActivator = addNewNodeLegendEditorActivator;
        this.setting = null;
        NODE_COLOR_LEGEND_EDITOR_LIST.add(this);

        setSpeciesField(getSpeciesOptions(), null);
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

    private boolean checkCurrentNetwork() {
        if (currentNetwork != null) return true;
        currentNetwork = manager.data.getCurrentNetwork();
        return currentNetwork != null;
    }

    private Vector<String> getSpeciesOptions() {
        Vector<String> options = new Vector<>();
        if (checkCurrentNetwork()) options.addAll(currentNetwork.getNonDefinedTaxon());
        if (setting != null) options.add(setting.getTaxonName());
        return options;
    }

    private void setSpeciesField(Vector<String> speciesOptions, String selectedOption) {
        speciesModel = new SortedComboBoxModel<>(speciesOptions);
        if (selectedOption != null) speciesModel.setSelectedItem(selectedOption);
        speciesField = new JComboBox<>(speciesModel);
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
        Color formerColor = (Color) StyleMapper.getKingdomColor(currentTaxId);
        if (formerColor != null)
            manager.style.updateStylesColorScheme(currentTaxId, formerColor, false, true);
        StyleMapper.speciesColors.remove(currentTaxId);
        manager.style.settings.removeUserColorSetting(currentTaxId);

    }

    private void setRemoveButton() {
        removeButton.addActionListener(e -> this.destroy(true));
        removeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        removeButton.setSize(30, 30);
    }

    private void updateStyleColors() {
        manager.style.updateStylesColorScheme(currentTaxId, currentColor, false, true);
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
        speciesModel.removeIf(speciesName -> !speciesName.equals(getSelectedTaxon()) && !currentNetwork.speciesExist(speciesName));
        getSpeciesOptions().forEach(speciesModel::addElement);
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

    public String getSelectedTaxId() {
        if (checkCurrentNetwork()) return currentNetwork.getSpeciesId(getSelectedTaxon());
        else if (setting != null) return setting.getTaxId();
        return null;
    }

    @Override
    public String getTaxId() {
        return getSelectedTaxId();
    }

    public String getSelectedTaxon() {
        if (speciesField == null) return null;
        return (String) speciesField.getSelectedItem();
    }

    @Override
    public String getDescriptor() {
        return getSelectedTaxon();
    }

    public static Set<String> getDefinedTaxIds() {
        return NODE_COLOR_LEGEND_EDITOR_LIST.stream().map(NodeColorLegendEditor::getSelectedTaxId).collect(Collectors.toSet());
    }
}
