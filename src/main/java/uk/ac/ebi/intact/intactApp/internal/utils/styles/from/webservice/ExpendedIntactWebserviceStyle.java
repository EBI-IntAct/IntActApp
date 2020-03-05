package uk.ac.ebi.intact.intactApp.internal.utils.styles.from.webservice;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

public class ExpendedIntactWebserviceStyle extends IntactWebserviceStyle {
    public ExpendedIntactWebserviceStyle(IntactManager manager) {
        super(manager, "IntAct - Evidence - Webservice");
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        DiscreteMapping<String, LineType> dMapping = (DiscreteMapping) discreteFactory.createVisualMappingFunction("style::shape", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        dMapping.putMapValue("solid", LineTypeVisualProperty.SOLID);
        dMapping.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(dMapping);
    }

    @Override
    protected void setEdgePaintStyle() {
//        FunctionalMapping<String, Paint> fMapping = (FunctionalMapping) functionalFactory.createVisualMappingFunction("style::color", String.class, BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
//        fMapping.setFunction(ModelUtils::parseColorRGB);
//        style.addVisualMappingFunction(fMapping);
    }
}
