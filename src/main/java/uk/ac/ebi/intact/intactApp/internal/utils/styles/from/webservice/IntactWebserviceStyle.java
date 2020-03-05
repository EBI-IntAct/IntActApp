package uk.ac.ebi.intact.intactApp.internal.utils.styles.from.webservice;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.styles.IntactStyle;

import java.awt.*;

public abstract class IntactWebserviceStyle extends IntactStyle {

    public IntactWebserviceStyle(IntactManager manager, String styleName) {
        super(manager, styleName);
    }

    @Override
    protected void setNodePaintStyle() {
//        FunctionalMapping<String, Paint> fMapping = (FunctionalMapping) functionalFactory.createVisualMappingFunction("style::color", String.class, BasicVisualLexicon.NODE_FILL_COLOR);
//        fMapping.setFunction(ModelUtils::parseColorRGB);
//        style.addVisualMappingFunction(fMapping);
        PassthroughMapping<String, Paint> fMapping = (PassthroughMapping<String, Paint>) passthroughFactory.createVisualMappingFunction("style::color", String.class, BasicVisualLexicon.NODE_FILL_COLOR);

        style.addVisualMappingFunction(fMapping);

    }

    @Override
    protected void setNodeBorderPaintStyle() {
//        FunctionalMapping<String, Paint> fMapping = (FunctionalMapping) functionalFactory.createVisualMappingFunction("style::color", String.class, BasicVisualLexicon.NODE_BORDER_PAINT);
//        fMapping.setFunction(ModelUtils::parseColorRGB);
        PassthroughMapping<String, Paint> fMapping = (PassthroughMapping<String, Paint>) passthroughFactory.createVisualMappingFunction("style::color", String.class, BasicVisualLexicon.NODE_BORDER_PAINT);

        style.addVisualMappingFunction(fMapping);
    }

    @Override
    protected void setNodeShapeStyle() {
        PassthroughMapping<String, NodeShape> pMapping = (PassthroughMapping<String, NodeShape>) passthroughFactory.createVisualMappingFunction("style::shape", String.class, BasicVisualLexicon.NODE_SHAPE);

        style.addVisualMappingFunction(pMapping);
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        DiscreteMapping<String, LineType> dMapping = (DiscreteMapping<String, LineType>) discreteFactory.createVisualMappingFunction("style::shape", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        dMapping.putMapValue("solid", LineTypeVisualProperty.SOLID);
        dMapping.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(dMapping);
    }
}
