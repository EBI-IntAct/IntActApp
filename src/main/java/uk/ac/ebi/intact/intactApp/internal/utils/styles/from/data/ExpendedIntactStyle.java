package uk.ac.ebi.intact.intactApp.internal.utils.styles.from.data;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.styles.IntactStyle;

public class ExpendedIntactStyle extends IntactStyle {

    public ExpendedIntactStyle(IntactManager manager) {
        super(manager, "Intact - Evidence");
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        //TODO Move to data instead of webservice infos
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
