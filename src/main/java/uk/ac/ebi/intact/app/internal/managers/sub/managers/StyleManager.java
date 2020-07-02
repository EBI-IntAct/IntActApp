package uk.ac.ebi.intact.app.internal.managers.sub.managers;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.managers.Manager;
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
    final Map<NetworkView.Type, Style> intactStyles = new HashMap<>();

    public StyleManager(Manager manager) {
        this.manager = manager;
    }

    public void setupStyles() {
        StyleMapper.initializeTaxIdToPaint();
        StyleMapper.initializeEdgeTypeToPaint();
        StyleMapper.initializeNodeTypeToShape();
        Style summary = new SummaryStyle(manager);
        Style expanded = new ExpandedStyle(manager);
        Style mutation = new MutationStyle(manager);

        for (Style style : new Style[]{summary, expanded, mutation}) {
            intactStyles.put(style.getStyleViewType(), style);
        }
    }

    public Map<NetworkView.Type, Style> getIntactStyles() {
        return new HashMap<>(intactStyles);
    }

    public void toggleFancyStyles() {
        for (Style style : intactStyles.values()) {
            style.toggleFancy();
        }
    }

    public void updateStylesColorScheme(Long parentTaxId, Color newColor, boolean addDescendants) {
        Map<Long, Paint> colorScheme = StyleMapper.updateChildrenColors(parentTaxId, newColor, addDescendants);
        for (Style style : intactStyles.values()) {
            style.updateTaxIdToNodePaintMapping(colorScheme);
        }
    }

    public void resetStyles() {
        StyleMapper.resetMappings();
        for (Style style : intactStyles.values()) {
            style.setNodePaintStyle();
        }
        for (Network network : manager.data.networkMap.values()) {
            network.completeMissingNodeColorsFromTables();
        }
    }
}