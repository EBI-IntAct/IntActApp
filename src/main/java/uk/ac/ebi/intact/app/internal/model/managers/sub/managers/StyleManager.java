package uk.ac.ebi.intact.app.internal.model.managers.sub.managers;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.SummaryStyle;
import uk.ac.ebi.intact.app.internal.model.styles.ExpandedStyle;
import uk.ac.ebi.intact.app.internal.model.styles.Style;
import uk.ac.ebi.intact.app.internal.model.styles.MutationStyle;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class StyleManager {
    private final Manager manager;
    final Map<NetworkView.Type, Style> styles = new HashMap<>();
    final VisualMappingManager vmm;

    public StyleManager(Manager manager) {
        this.manager = manager;
        vmm = manager.utils.getService(VisualMappingManager.class);
    }

    public void setupStyles() {
        StyleMapper.initializeTaxIdToPaint();
        StyleMapper.initializeEdgeTypeToPaint();
        StyleMapper.initializeNodeTypeToShape();
        Style summary = new SummaryStyle(manager);
        Style expanded = new ExpandedStyle(manager);
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
        for (Style style: styles.values()) {
            if (style.getStyle() == visualStyle) return style;
        }
        return null;
    }

    public void toggleFancyStyles() {
        for (Style style : styles.values()) {
            style.toggleFancy();
        }
    }

    public void updateStylesColorScheme(Long parentTaxId, Color newColor, boolean addDescendants) {
        Map<Long, Paint> colorScheme = StyleMapper.updateChildrenColors(parentTaxId, newColor, addDescendants);
        for (Style style : styles.values()) {
            style.updateTaxIdToNodePaintMapping(colorScheme);
        }
    }

    public void resetStyles() {
        StyleMapper.resetMappings();
        for (Style style : styles.values()) {
            style.setNodePaintStyle();
        }
        for (Network network : manager.data.networkMap.values()) {
            network.completeMissingNodeColorsFromTables();
        }
    }
}