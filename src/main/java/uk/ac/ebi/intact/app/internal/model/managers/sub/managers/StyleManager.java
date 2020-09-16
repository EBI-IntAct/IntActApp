package uk.ac.ebi.intact.app.internal.model.managers.sub.managers;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings.ColorSettingManager;
import uk.ac.ebi.intact.app.internal.model.styles.EvidenceStyle;
import uk.ac.ebi.intact.app.internal.model.styles.MutationStyle;
import uk.ac.ebi.intact.app.internal.model.styles.Style;
import uk.ac.ebi.intact.app.internal.model.styles.SummaryStyle;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StyleManager {
    private final Manager manager;
    final Map<NetworkView.Type, Style> styles = new HashMap<>();
    final VisualMappingManager vmm;
    public final ColorSettingManager settings;

    public StyleManager(Manager manager) {
        this.manager = manager;
        vmm = manager.utils.getService(VisualMappingManager.class);
        settings = new ColorSettingManager(manager);
        manager.utils.registerAllServices(this, new Properties());
    }

    public void setupStyles() {
        StyleMapper.initializeSpeciesAndKingdomColors(true);
        StyleMapper.initializeEdgeTypeToPaint();
        StyleMapper.initializeNodeTypeToShape();

        Style summary = new SummaryStyle(manager);
        Style expanded = new EvidenceStyle(manager);
        Style mutation = new MutationStyle(manager);

        for (Style style : new Style[]{summary, expanded, mutation}) {
            styles.put(style.getStyleViewType(), style);
        }
    }

    public Map<NetworkView.Type, Style> getStyles() {
        return new HashMap<>(styles);
    }

    public Style getStyle(NetworkView view) {
        VisualStyle visualStyle = vmm.getVisualStyle(view.cyView);
        for (Style style : styles.values()) {
            if (style.getStyle() == visualStyle) return style;
        }
        return null;
    }

    public Style getStyle(NetworkView.Type type) {
        return styles.get(type);
    }

    public void toggleFancyStyles() {
        for (Style style : styles.values()) {
            style.toggleFancy();
        }
    }

    public void setStylesFancy(boolean fancy) {
        for (Style style : styles.values()) {
            style.setFancy(fancy);
        }
    }

    public void updateStylesColorScheme(String parentTaxId, Color newColor, boolean addDescendants, boolean isKingdom) {
        Map<String, Paint> colorScheme = StyleMapper.updateChildrenColors(parentTaxId, newColor, addDescendants, isKingdom);
        for (Style style : styles.values()) {
            style.updateTaxIdToNodePaintMapping(colorScheme);
        }
    }

    public void resetStyles(boolean async) {
        settings.resetSettings();
        StyleMapper.resetMappings(async);
        for (Style style : styles.values()) {
            style.setNodePaintStyle();
        }
        for (Network network : manager.data.networkMap.values()) {
            network.completeMissingNodeColorsFromTables(async, null);
        }
        StyleMapper.fireStyleUpdated();
    }

    void hardResetStyles() {
        styles.values().forEach(style -> vmm.removeVisualStyle(style.getStyle()));
        styles.clear();
        setupStyles();
    }
}