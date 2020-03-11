package uk.ac.ebi.intact.intactApp.internal.utils.styles.from.webservice;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.awt.*;

public class ExpendedIntactWebserviceStyle extends IntactWebserviceStyle {
    public ExpendedIntactWebserviceStyle(IntactManager manager) {
        super(manager, "IntAct - Evidence - Webservice");
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        DiscreteMapping<String, LineType> dMapping = (DiscreteMapping<String, LineType>) discreteFactory.createVisualMappingFunction("style::shape", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        dMapping.putMapValue("solid", LineTypeVisualProperty.SOLID);
        dMapping.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(dMapping);
    }

    @Override
    protected void setEdgePaintStyle() {
        PassthroughMapping<String, Paint> colorToEdgeColor = (PassthroughMapping<String, Paint>) passthroughFactory.createVisualMappingFunction(ModelUtils.COLOR, String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);

        style.addVisualMappingFunction(colorToEdgeColor);
    }
}
