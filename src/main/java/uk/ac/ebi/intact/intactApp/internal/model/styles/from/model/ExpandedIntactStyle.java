package uk.ac.ebi.intact.intactApp.internal.model.styles.from.model;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.styles.IntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.OLSMapper;
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
        //TODO Move to data instead of webservice infos
        DiscreteMapping<String, LineType> shapeToLineType = (DiscreteMapping<String, LineType>) discreteFactory.createVisualMappingFunction(ModelUtils.SHAPE, String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        shapeToLineType.putMapValue("solid", LineTypeVisualProperty.SOLID);
        shapeToLineType.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(shapeToLineType);
    }

    @Override
    protected void setEdgePaintStyle() {
        DiscreteMapping<String, Paint> interactionTypeToColor = (DiscreteMapping<String, Paint>) discreteFactory.createVisualMappingFunction(CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        interactionTypeToColor.putAll(OLSMapper.edgeTypeToPaint);
        style.addVisualMappingFunction(interactionTypeToColor);
        style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(153, 153, 153));
        addMissingEdgePaint(interactionTypeToColor);
    }

    private void addMissingEdgePaint(DiscreteMapping<String, Paint> interactionTypeToColor) {
        new Thread(() -> {
            OLSMapper.initializeEdgeTypeToPaint();
            while (OLSMapper.edgeTypesNotReady()) {
                TimeUtils.sleep(100);
            }
            interactionTypeToColor.putAll(OLSMapper.edgeTypeToPaint);
        }).start();

    }


    @Override
    public String getStyleName() {
        return TITLE;
    }
}
