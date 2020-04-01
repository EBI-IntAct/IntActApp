package uk.ac.ebi.intact.intactApp.internal.model.styles.from.webservice;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.model.styles.IntactStyle;

import java.awt.*;

public abstract class IntactWebserviceStyle extends IntactStyle {

    public IntactWebserviceStyle(IntactManager manager) {
        super(manager);
    }

    @Override
    public void setNodePaintStyle() {
        PassthroughMapping<String, Paint> colorToNodeColor = (PassthroughMapping<String, Paint>) passthroughFactory.createVisualMappingFunction(ModelUtils.COLOR, String.class, BasicVisualLexicon.NODE_FILL_COLOR);

        style.addVisualMappingFunction(colorToNodeColor);

    }

    @Override
    protected void setNodeBorderPaintStyle() {
        PassthroughMapping<String, Paint> colorToNodeBorderColor = (PassthroughMapping<String, Paint>) passthroughFactory.createVisualMappingFunction(ModelUtils.COLOR, String.class, BasicVisualLexicon.NODE_BORDER_PAINT);

        style.addVisualMappingFunction(colorToNodeBorderColor);
    }

    @Override
    protected void setNodeShapeStyle() {
        PassthroughMapping<String, NodeShape> shapeToNodeShape = (PassthroughMapping<String, NodeShape>) passthroughFactory.createVisualMappingFunction(ModelUtils.SHAPE, String.class, BasicVisualLexicon.NODE_SHAPE);

        style.addVisualMappingFunction(shapeToNodeShape);
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        DiscreteMapping<String, LineType> dMapping = (DiscreteMapping<String, LineType>) discreteFactory.createVisualMappingFunction(ModelUtils.SHAPE, String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        dMapping.putMapValue("solid", LineTypeVisualProperty.SOLID);
        dMapping.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(dMapping);
    }
}
