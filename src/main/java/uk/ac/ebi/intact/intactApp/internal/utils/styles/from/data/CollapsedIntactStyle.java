package uk.ac.ebi.intact.intactApp.internal.utils.styles.from.data;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.styles.FunctionalMapping.FunctionalMapping;
import uk.ac.ebi.intact.intactApp.internal.utils.styles.IntactStyle;

import java.awt.*;

public class CollapsedIntactStyle extends IntactStyle {
    public CollapsedIntactStyle(IntactManager manager) {
        super(manager, "Intact - Collapsed");
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        style.setDefaultValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.SOLID);
    }

    @Override
    protected void setEdgePaintStyle() {
        FunctionalMapping<Double, Paint> fMapping = (FunctionalMapping) functionalFactory.createVisualMappingFunction(ModelUtils.MI_SCORE, Double.class, BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
        fMapping.setFunction(miScore -> {
            if (miScore < 0.1) {
                return new Color(255, 255, 221);
            } else if (miScore < 0.2) {
                return new Color(255, 248, 171);
            } else if (miScore < 0.3) {
                return new Color(254, 224, 121);
            } else if (miScore < 0.4) {
                return new Color(254, 186, 51);
            } else if (miScore < 0.5) {
                return new Color(254, 135, 13);
            } else if (miScore < 0.6) {
                return new Color(231, 90, 0);
            } else if (miScore < 0.7) {
                return new Color(193, 55, 0);
            } else if (miScore < 0.8) {
                return new Color(135, 36, 0);
            } else if (miScore < 0.9) {
                return new Color(83, 26, 0);
            } else {
                return new Color(41, 15, 2);
            }
        });

        style.addVisualMappingFunction(fMapping);
    }
}
