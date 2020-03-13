package uk.ac.ebi.intact.intactApp.internal.utils.styles.from.model;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.styles.IntactStyle;

import java.awt.*;

public class CollapsedIntactStyle extends IntactStyle {
    public final static String TITLE = "Intact - Collapsed";

    public CollapsedIntactStyle(IntactManager manager) {
        super(manager);
    }

    private static Color[] colors = {
            new Color(255, 255, 221),
            new Color(255, 255, 221),
            new Color(255, 248, 171),
            new Color(254, 224, 121),
            new Color(254, 186, 51),
            new Color(254, 135, 13),
            new Color(231, 90, 0),
            new Color(193, 55, 0),
            new Color(135, 36, 0),
            new Color(83, 26, 0),
            new Color(41, 15, 2),
            new Color(41, 15, 2)
    };

    @Override
    protected void setEdgeLineTypeStyle() {
        style.setDefaultValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.SOLID);
    }

    @Override
    protected void setEdgePaintStyle() {
        ContinuousMapping<Double, Paint> miScoreToEdgeColor = (ContinuousMapping<Double, Paint>) continuousFactory.createVisualMappingFunction(ModelUtils.C_MI_SCORE, Double.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        for (int i = 0; i < colors.length - 1; i++) {
            miScoreToEdgeColor.addPoint(i / 10.0, new BoundaryRangeValues<>(colors[i], colors[i], colors[i + 1]));
            miScoreToEdgeColor.addPoint((i / 10.0) + 0.001, new BoundaryRangeValues<>(colors[i+1], colors[i+1], colors[i + 1]));
        }
        style.addVisualMappingFunction(miScoreToEdgeColor);
    }

    @Override
    public String getStyleName() {
        return TITLE;
    }
}
