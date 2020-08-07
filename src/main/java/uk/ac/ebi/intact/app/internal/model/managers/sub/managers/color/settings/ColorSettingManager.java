package uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.session.CySession;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings.events.ColorSettingLoadedEvent;
import uk.ac.ebi.intact.app.internal.model.styles.Style;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.ui.components.legend.NodeColorLegendEditor;
import uk.ac.ebi.intact.app.internal.ui.components.legend.NodeColorPicker;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.app.internal.model.tables.Table.STYLE_SETTINGS;
import static uk.ac.ebi.intact.app.internal.model.tables.fields.enums.StyleSettingsFields.JSON_VALUE;

public class ColorSettingManager implements NodeColorPicker.ColorChangedListener {
    private final Manager manager;
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
    private CyTable table;
    public final static String TITLE = "IntAct - Custom Styles";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SPECIES_KEY = "speciesColors";
    private final Set<ColorSetting> speciesSettings = new HashSet<>();
    private static final String KINGDOM_KEY = "kingdomColors";
    private final Set<ColorSetting> kingdomSettings = new HashSet<>();
    private static final String USER_KEY = "userColors";
    private final Set<ColorSetting> userSettings = new HashSet<>();
    private CyTableManager tableManager;

    public ColorSettingManager(Manager manager) {
        this.manager = manager;
        tableManager = manager.utils.getService(CyTableManager.class);
        table = STYLE_SETTINGS.build(manager, TITLE);
        tableManager.addTable(table);
        resetSettings();
    }

    @Override
    public void colorChanged(NodeColorPicker.ColorChangedEvent e) {
        NodeColorPicker source = e.getSource();
        if (source instanceof NodeColorLegendEditor) {
            var editor = (NodeColorLegendEditor) source;
            ColorSetting setting = new ColorSetting(editor.getSelectedTaxId(), editor.getSelectedTaxon(), e.newColor);
            userSettings.remove(setting);
            userSettings.add(setting);
            saveSettings(USER_KEY, userSettings);
        } else {
            ColorSetting setting = new ColorSetting(source.getTaxId(), source.getDescriptor(), e.newColor);
            if (source.isDefinedSpecies()) {
                speciesSettings.remove(setting);
                speciesSettings.add(setting);
                saveSettings(SPECIES_KEY, speciesSettings);
            } else {
                kingdomSettings.remove(setting);
                kingdomSettings.add(setting);
                saveSettings(KINGDOM_KEY, kingdomSettings);
            }
        }
    }

    public void resetSettings() {
        speciesSettings.clear();
        saveSettings(SPECIES_KEY, speciesSettings);
        kingdomSettings.clear();
        saveSettings(KINGDOM_KEY, kingdomSettings);
        userSettings.clear();
        saveSettings(USER_KEY, userSettings);
    }

    public void removeUserColorSetting(String taxId) {
        userSettings.removeIf(colorSetting -> colorSetting.taxId.equals(taxId));
        saveSettings(USER_KEY, userSettings);
    }

    private void saveSettings(String key, Set<ColorSetting> settings) {
        executor.execute(() -> JSON_VALUE.setValue(table.getRow(key), buildSave(settings)));
    }

    private String buildSave(Set<ColorSetting> settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void loadSessionSettings(CySession session) {
        tableManager.deleteTable(table.getSUID());
        table = STYLE_SETTINGS.getOrBuild(manager, TITLE, session.getTables().stream().map(CyTableMetadata::getTable).collect(Collectors.toList()));
        speciesSettings.addAll(loadSettings(SPECIES_KEY));
        kingdomSettings.addAll(loadSettings(KINGDOM_KEY));
        userSettings.addAll(loadSettings(USER_KEY));
        applySettingsToStyle();
        manager.utils.fireEvent(new ColorSettingLoadedEvent(this));
    }

    public void applySettingsToStyle() {
        Map<String, Paint> colorScheme = new HashMap<>();
        StyleMapper.harvestKingdomsOf(userSettings.stream().map(ColorSetting::getTaxId).collect(Collectors.toSet()), false);
        speciesSettings.forEach(colorSetting -> colorScheme.putAll(StyleMapper.updateChildrenColors(colorSetting.taxId, colorSetting.color, true, false)));
        kingdomSettings.forEach(colorSetting -> colorScheme.putAll(StyleMapper.updateChildrenColors(colorSetting.taxId, colorSetting.color, true, true)));
        userSettings.forEach(colorSetting -> colorScheme.putAll(StyleMapper.updateChildrenColors(colorSetting.taxId, colorSetting.color, false, true)));
        for (Style style : manager.style.getStyles().values()) {
            style.updateTaxIdToNodePaintMapping(colorScheme);
        }
    }

    private Set<ColorSetting> loadSettings(String key) {
        try {
            String json = JSON_VALUE.getValue(table.getRow(key));
            if (json != null) return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    public Set<ColorSetting> getSpeciesSettings() {
        return new HashSet<>(speciesSettings);
    }

    public Set<ColorSetting> getKingdomSettings() {
        return new HashSet<>(kingdomSettings);
    }

    public Set<ColorSetting> getUserSettings() {
        return new HashSet<>(userSettings);
    }
}
