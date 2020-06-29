package uk.ac.ebi.intact.app.internal.model.styles;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.models.EdgeFields;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.models.NodeFields;

import java.awt.*;

public class MutationIntactStyle extends ExpandedIntactStyle {

    public static final String TITLE = "Intact - Mutation";
    public final static NetworkView.Type type = NetworkView.Type.MUTATION;
    public static final Color mutatedColor = new Color(255, 0, 161);
    public static final Color wildColor = new Color(126, 131, 137);

    public MutationIntactStyle(Manager manager) {
        super(manager);
    }

    @Override
    protected void setNodeBorderPaintStyle() {
        DiscreteMapping<Boolean, Paint> mutationToNodeBorder = (DiscreteMapping<Boolean, Paint>) discreteFactory.createVisualMappingFunction(NodeFields.MUTATED.toString(), Boolean.class, BasicVisualLexicon.NODE_BORDER_PAINT);
        mutationToNodeBorder.putMapValue(true, mutatedColor);

        style.addVisualMappingFunction(mutationToNodeBorder);
        style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, wildColor);
    }

    @Override
    protected void setNodeBorderWidth() {
        DiscreteMapping<Boolean, Double> mutationToNodeBorderWidth = (DiscreteMapping<Boolean, Double>) discreteFactory.createVisualMappingFunction(NodeFields.MUTATED.toString(), Boolean.class, BasicVisualLexicon.NODE_BORDER_WIDTH);
        mutationToNodeBorderWidth.putMapValue(true, 8.0);
        mutationToNodeBorderWidth.putMapValue(false, 0.0);

        style.addVisualMappingFunction(mutationToNodeBorderWidth);
        style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0.0);
    }


    @Override
    protected void setEdgePaintStyle() {
        DiscreteMapping<Boolean, Paint> disruptedToNodeColor = (DiscreteMapping<Boolean, Paint>) discreteFactory.createVisualMappingFunction(EdgeFields.AFFECTED_BY_MUTATION.toString(), Boolean.class, BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
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
        DiscreteMapping<Boolean, Double> disruptedToNodeBorderWidth = (DiscreteMapping<Boolean, Double>) discreteFactory.createVisualMappingFunction(EdgeFields.AFFECTED_BY_MUTATION.toString(), Boolean.class, BasicVisualLexicon.EDGE_WIDTH);
        disruptedToNodeBorderWidth.putMapValue(true, 4.0);
        disruptedToNodeBorderWidth.putMapValue(false, 1.0);

        style.addVisualMappingFunction(disruptedToNodeBorderWidth);
    }

    @Override
    public String getStyleName() {
        return TITLE;
    }

    @Override
    public NetworkView.Type getStyleViewType() {
        return type;
    }


}
