package uk.ac.ebi.intact.intactApp.internal.utils.styles.from.model;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.styles.from.webservice.IntactWebserviceStyle;

import java.awt.*;

public class MutationIntactStyle extends IntactWebserviceStyle {
    public MutationIntactStyle(IntactManager manager) {
        super(manager, "Intact - Mutation");
    }

    @Override
    protected void setNodeBorderPaintStyle() {
        DiscreteMapping<Boolean, Paint> mutationToNodeBorder = (DiscreteMapping<Boolean, Paint>) discreteFactory.createVisualMappingFunction(ModelUtils.MUTATION, Boolean.class, BasicVisualLexicon.NODE_BORDER_PAINT);
        mutationToNodeBorder.putMapValue(true, new Color(255, 0, 0));

        style.addVisualMappingFunction(mutationToNodeBorder);
    }

    @Override
    protected void setNodeBorderWidth() {
        DiscreteMapping<Boolean, Double> mutationToNodeBorderWidth = (DiscreteMapping<Boolean, Double>) discreteFactory.createVisualMappingFunction(ModelUtils.MUTATION, Boolean.class, BasicVisualLexicon.NODE_BORDER_WIDTH);
        mutationToNodeBorderWidth.putMapValue(true, 8.0);
        mutationToNodeBorderWidth.putMapValue(false, 0.0);

        style.addVisualMappingFunction(mutationToNodeBorderWidth);
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        DiscreteMapping<String, LineType> shapeToLineType = (DiscreteMapping<String, LineType>) discreteFactory.createVisualMappingFunction("style::shape", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        shapeToLineType.putMapValue("solid", LineTypeVisualProperty.SOLID);
        shapeToLineType.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(shapeToLineType);
    }

    @Override
    protected void setEdgePaintStyle() {
        DiscreteMapping<Boolean, Paint> disruptedToNodeColor = (DiscreteMapping<Boolean, Paint>) discreteFactory.createVisualMappingFunction(ModelUtils.DISRUPTED_BY_MUTATION, Boolean.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        disruptedToNodeColor.putMapValue(true, new Color(255, 0, 0));
        disruptedToNodeColor.putMapValue(false, new Color(126, 131, 137));

        style.addVisualMappingFunction(disruptedToNodeColor);
    }

    @Override
    protected void setEdgeWidth() {
        DiscreteMapping<Boolean, Double> disruptedToNodeBorderWidth = (DiscreteMapping<Boolean, Double>) discreteFactory.createVisualMappingFunction(ModelUtils.DISRUPTED_BY_MUTATION, Boolean.class, BasicVisualLexicon.EDGE_WIDTH);
        disruptedToNodeBorderWidth.putMapValue(true, 4.0);
        disruptedToNodeBorderWidth.putMapValue(false, 1.0);

        style.addVisualMappingFunction(disruptedToNodeBorderWidth);
    }
}
