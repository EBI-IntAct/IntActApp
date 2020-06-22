package uk.ac.ebi.intact.intactApp.internal.model.managers.sub.managers;

import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.styles.CollapsedIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.ExpandedIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.IntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.MutationIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class IntactStyleManager {
    private final IntactManager intactManager;
    final Map<IntactNetworkView.Type, IntactStyle> intactStyles = new HashMap<>();

    public IntactStyleManager(IntactManager intactManager) {
        this.intactManager = intactManager;
    }

    public void setupStyles() {
        StyleMapper.initializeTaxIdToPaint();
        StyleMapper.initializeEdgeTypeToPaint();
        StyleMapper.initializeNodeTypeToShape();
        IntactStyle collapsed = new CollapsedIntactStyle(intactManager);
        IntactStyle expanded = new ExpandedIntactStyle(intactManager);
        IntactStyle mutation = new MutationIntactStyle(intactManager);

        for (IntactStyle style : new IntactStyle[]{collapsed, expanded, mutation}) {
            intactStyles.put(style.getStyleViewType(), style);
        }
    }

    public Map<IntactNetworkView.Type, IntactStyle> getIntactStyles() {
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
        for (IntactNetwork network : intactManager.data.intactNetworkMap.values()) {
            network.completeMissingNodeColorsFromTables();
        }
    }
}