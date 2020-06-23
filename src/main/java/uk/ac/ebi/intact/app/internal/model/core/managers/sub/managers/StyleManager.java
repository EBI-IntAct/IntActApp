package uk.ac.ebi.intact.app.internal.model.core.managers.sub.managers;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.CollapsedIntactStyle;
import uk.ac.ebi.intact.app.internal.model.styles.ExpandedIntactStyle;
import uk.ac.ebi.intact.app.internal.model.styles.IntactStyle;
import uk.ac.ebi.intact.app.internal.model.styles.MutationIntactStyle;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class StyleManager {
    private final Manager manager;
    final Map<NetworkView.Type, IntactStyle> intactStyles = new HashMap<>();

    public StyleManager(Manager manager) {
        this.manager = manager;
    }

    public void setupStyles() {
        StyleMapper.initializeTaxIdToPaint();
        StyleMapper.initializeEdgeTypeToPaint();
        StyleMapper.initializeNodeTypeToShape();
        IntactStyle collapsed = new CollapsedIntactStyle(manager);
        IntactStyle expanded = new ExpandedIntactStyle(manager);
        IntactStyle mutation = new MutationIntactStyle(manager);

        for (IntactStyle style : new IntactStyle[]{collapsed, expanded, mutation}) {
            intactStyles.put(style.getStyleViewType(), style);
        }
    }

    public Map<NetworkView.Type, IntactStyle> getIntactStyles() {
        return new HashMap<>(intactStyles);
    }

    public void toggleFancyStyles() {
        for (IntactStyle style : intactStyles.values()) {
            style.toggleFancy();
        }
    }

    public void updateStylesColorScheme(Long parentTaxId, Color newColor, boolean addDescendants) {
        Map<Long, Paint> colorScheme = StyleMapper.updateChildrenColors(parentTaxId, newColor, addDescendants);
        for (IntactStyle style : intactStyles.values()) {
            style.updateTaxIdToNodePaintMapping(colorScheme);
        }
    }

    public void resetStyles() {
        StyleMapper.resetMappings();
        for (IntactStyle style : intactStyles.values()) {
            style.setNodePaintStyle();
        }
        for (Network network : manager.data.networkMap.values()) {
            network.completeMissingNodeColorsFromTables();
        }
    }
}