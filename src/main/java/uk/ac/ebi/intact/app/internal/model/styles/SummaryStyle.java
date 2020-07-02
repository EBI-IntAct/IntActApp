package uk.ac.ebi.intact.app.internal.model.styles;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields;

import java.awt.*;

public class SummaryStyle extends Style {
    public final static String TITLE = "Intact - Summary";
    public final static NetworkView.Type type = NetworkView.Type.SUMMARY;
    public static final int edgeWidthValue1 = 1;
    public static final int edgeWidthValue2 = 25;
    public static final double edgeWidth1 = 2d;
    public static final double edgeWidth2 = 25d;

    public SummaryStyle(Manager manager) {
        super(manager);
    }

    public static final Color[] colors = {
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
    protected void setEdgeWidth() {
        ContinuousMapping<Integer, Double> nbSummarizedEdgesToThickness = (ContinuousMapping<Integer, Double>) continuousFactory.createVisualMappingFunction(EdgeFields.SUMMARY_NB_EDGES.toString(), Integer.class, BasicVisualLexicon.EDGE_WIDTH);

        nbSummarizedEdgesToThickness.addPoint(edgeWidthValue1, new BoundaryRangeValues<>(edgeWidth1, edgeWidth1, edgeWidth1));
        nbSummarizedEdgesToThickness.addPoint(edgeWidthValue2, new BoundaryRangeValues<>(edgeWidth2, edgeWidth2, edgeWidth2));
        style.addVisualMappingFunction(nbSummarizedEdgesToThickness);
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        style.setDefaultValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.SOLID);
    }

    @Override
    protected void setEdgePaintStyle() {
        ContinuousMapping<Double, Paint> miScoreToEdgeColor = (ContinuousMapping<Double, Paint>) continuousFactory.createVisualMappingFunction(EdgeFields.MI_SCORE.toString(), Double.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        for (int i = 0; i < colors.length - 1; i++) {
            miScoreToEdgeColor.addPoint(i / 10.0, new BoundaryRangeValues<>(colors[i], colors[i], colors[i + 1]));
            miScoreToEdgeColor.addPoint((i / 10.0) + 0.001, new BoundaryRangeValues<>(colors[i + 1], colors[i + 1], colors[i + 1]));
        }

        style.addVisualMappingFunction(miScoreToEdgeColor);
    }

    @Override
    public String getStyleName() {
        return TITLE;
    }

    @Override
    public NetworkView.Type getStyleViewType() {
        return type;
    }


    public static Color getColor(double miScore) {
        return colors[1 + (int) Math.floor(miScore * 10)];
    }
}
