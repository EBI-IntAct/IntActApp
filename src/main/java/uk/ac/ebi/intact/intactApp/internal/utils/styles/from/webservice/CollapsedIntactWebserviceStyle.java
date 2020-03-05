package uk.ac.ebi.intact.intactApp.internal.utils.styles.from.webservice;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

public class CollapsedIntactWebserviceStyle extends IntactWebserviceStyle {
    public CollapsedIntactWebserviceStyle(IntactManager manager) {
        super(manager, "Intact - Collapsed - Webservice");
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        style.setDefaultValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.SOLID);
    }
    @Override
    protected void setEdgePaintStyle() {
//        FunctionalMapping<String, Paint> fMapping = (FunctionalMapping) functionalFactory.createVisualMappingFunction("style::collapsed_color", String.class, BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
//        fMapping.setFunction(ModelUtils::parseColorRGB);
//
//        style.addVisualMappingFunction(fMapping);
    }
}
