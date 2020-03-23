package uk.ac.ebi.intact.intactApp.internal.model.styles.from.model;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.styles.IntactStyle;

public class ExpandedIntactStyle extends IntactStyle {

    public static final String TITLE = "Intact - Evidence";

    public ExpandedIntactStyle(IntactManager manager) {
        super(manager);
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        //TODO Move to data instead of webservice infos
        DiscreteMapping<String, LineType> shapeToLineType = (DiscreteMapping<String, LineType>) discreteFactory.createVisualMappingFunction("style::shape", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        shapeToLineType.putMapValue("solid", LineTypeVisualProperty.SOLID);
        shapeToLineType.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(shapeToLineType);
    }

    @Override
    protected void setEdgePaintStyle() {
//        FunctionalMapping<String, Paint> fMapping = (FunctionalMapping) functionalFactory.createVisualMappingFunction("style::color", String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
//        fMapping.setFunction(ModelUtils::parseColorRGB);
//        style.addVisualMappingFunction(fMapping);
    }

    @Override
    public String getStyleName() {
        return TITLE;
    }
}
