package uk.ac.ebi.intact.intactApp.internal.model.styles;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.awt.*;

public class MutationIntactStyle extends ExpandedIntactStyle {

    public static final String TITLE = "Intact - Mutation";
    public final static IntactNetworkView.Type type = IntactNetworkView.Type.MUTATION;
    public static final Color mutatedColor = new Color(255, 0, 161);
    public static final Color wildColor = new Color(126, 131, 137);

    public MutationIntactStyle(IntactManager manager) {
        super(manager);
    }

    @Override
    protected void setNodeBorderPaintStyle() {
        DiscreteMapping<Boolean, Paint> mutationToNodeBorder = (DiscreteMapping<Boolean, Paint>) discreteFactory.createVisualMappingFunction(ModelUtils.MUTATION, Boolean.class, BasicVisualLexicon.NODE_BORDER_PAINT);
        mutationToNodeBorder.putMapValue(true, mutatedColor);

        style.addVisualMappingFunction(mutationToNodeBorder);
        style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, wildColor);
    }

    @Override
    protected void setNodeBorderWidth() {
        DiscreteMapping<Boolean, Double> mutationToNodeBorderWidth = (DiscreteMapping<Boolean, Double>) discreteFactory.createVisualMappingFunction(ModelUtils.MUTATION, Boolean.class, BasicVisualLexicon.NODE_BORDER_WIDTH);
        mutationToNodeBorderWidth.putMapValue(true, 8.0);
        mutationToNodeBorderWidth.putMapValue(false, 0.0);

        style.addVisualMappingFunction(mutationToNodeBorderWidth);
        style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0.0);
    }


    @Override
    protected void setEdgePaintStyle() {
        DiscreteMapping<Boolean, Paint> disruptedToNodeColor = (DiscreteMapping<Boolean, Paint>) discreteFactory.createVisualMappingFunction(ModelUtils.AFFECTED_BY_MUTATION, Boolean.class, BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
        disruptedToNodeColor.putMapValue(true, mutatedColor);
        disruptedToNodeColor.putMapValue(false, wildColor);
        style.addVisualMappingFunction(disruptedToNodeColor);
        style.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, wildColor);
        style.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, wildColor);


        for (VisualPropertyDependency<?> vpd : style.getAllVisualPropertyDependencies()) {
            if (vpd.getIdString().equals("arrowColorMatchesEdge"))
                vpd.setDependency(true);
        }

    }


    @Override
    protected void setEdgeWidth() {
        DiscreteMapping<Boolean, Double> disruptedToNodeBorderWidth = (DiscreteMapping<Boolean, Double>) discreteFactory.createVisualMappingFunction(ModelUtils.AFFECTED_BY_MUTATION, Boolean.class, BasicVisualLexicon.EDGE_WIDTH);
        disruptedToNodeBorderWidth.putMapValue(true, 4.0);
        disruptedToNodeBorderWidth.putMapValue(false, 1.0);

        style.addVisualMappingFunction(disruptedToNodeBorderWidth);
    }

//    @Override
//    protected void setEdgeSourceShape() {
//        PassthroughMapping<String, ArrowShape> valueToSourceShape = (PassthroughMapping<String, ArrowShape>) passthroughFactory.createVisualMappingFunction(ModelUtils.SOURCE_SHAPE, String.class, BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
//        style.setDefaultValue(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
//        style.addVisualMappingFunction(valueToSourceShape);
//    }
//
//    @Override
//    protected void setEdgeTargetShape() {
//        PassthroughMapping<String, ArrowShape> valueToTargetShape = (PassthroughMapping<String, ArrowShape>) passthroughFactory.createVisualMappingFunction(ModelUtils.TARGET_SHAPE, String.class, BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
//        style.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
//        style.addVisualMappingFunction(valueToTargetShape);
//    }

    @Override
    public String getStyleName() {
        return TITLE;
    }

    @Override
    public IntactNetworkView.Type getStyleViewType() {
        return type;
    }


}
