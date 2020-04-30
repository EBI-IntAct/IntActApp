package uk.ac.ebi.intact.intactApp.internal.model.styles;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TimeUtils;

import java.awt.*;

public class ExpandedIntactStyle extends IntactStyle {

    public static final String TITLE = "Intact - Evidence";

    public ExpandedIntactStyle(IntactManager manager) {
        super(manager);
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        DiscreteMapping<String, LineType> shapeToLineType = (DiscreteMapping<String, LineType>) discreteFactory.createVisualMappingFunction(ModelUtils.EXPANSION_TYPE, String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        shapeToLineType.putMapValue("null", LineTypeVisualProperty.SOLID);
        shapeToLineType.putMapValue("spoke expansion", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(shapeToLineType);
    }

    @Override
    protected void setEdgePaintStyle() {
        DiscreteMapping<String, Paint> interactionTypeToColor = (DiscreteMapping<String, Paint>) discreteFactory.createVisualMappingFunction(CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        interactionTypeToColor.putAll(StyleMapper.edgeTypeToPaint);
        style.addVisualMappingFunction(interactionTypeToColor);
        style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(153, 153, 153));
        addMissingEdgePaint(interactionTypeToColor);
    }

    private void addMissingEdgePaint(DiscreteMapping<String, Paint> interactionTypeToColor) {
        new Thread(() -> {
            StyleMapper.initializeEdgeTypeToPaint();
            while (StyleMapper.edgeTypesNotReady()) {
                TimeUtils.sleep(100);
            }
            interactionTypeToColor.putAll(StyleMapper.edgeTypeToPaint);
        }).start();

    }


    @Override
    public String getStyleName() {
        return TITLE;
    }
}
