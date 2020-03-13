package uk.ac.ebi.intact.intactApp.internal.utils.styles.from.webservice;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.awt.*;

public class CollapsedIntactWebserviceStyle extends IntactWebserviceStyle {

    public static final String TITLE = "Intact - Collapsed - Webservice";

    public CollapsedIntactWebserviceStyle(IntactManager manager) {
        super(manager);
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        style.setDefaultValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.SOLID);
    }
    @Override
    protected void setEdgePaintStyle() {
        PassthroughMapping<String, Paint> colorToEdgeColor = (PassthroughMapping<String, Paint>) passthroughFactory.createVisualMappingFunction(ModelUtils.C_COLOR, String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);

        style.addVisualMappingFunction(colorToEdgeColor);
    }

    @Override
    public String getStyleName() {
        return TITLE;
    }
}
