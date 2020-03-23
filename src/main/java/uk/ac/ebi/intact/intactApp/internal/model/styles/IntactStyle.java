package uk.ac.ebi.intact.intactApp.internal.model.styles;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.awt.*;

public abstract class IntactStyle {
    protected VisualStyle style;
    protected IntactManager manager;
    protected CyEventHelper eventHelper;
    protected VisualMappingManager vmm;
    protected VisualMappingFunctionFactory continuousFactory;
    protected VisualMappingFunctionFactory discreteFactory;
    protected VisualMappingFunctionFactory passthroughFactory;

    private boolean newStyle;

    public IntactStyle(IntactManager manager) {
        this.manager = manager;
        vmm = manager.getService(VisualMappingManager.class);
        eventHelper = manager.getService(CyEventHelper.class);
        style = getOrCreateStyle();
        continuousFactory = manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
        discreteFactory = manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        passthroughFactory = manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        createStyle();
        if (newStyle)
            registerStyle();
    }

    private VisualStyle getOrCreateStyle() {
        for (VisualStyle createdStyle : vmm.getAllVisualStyles()) {
            if (createdStyle.getTitle().equals(getStyleName())) {
                newStyle = false;
                return createdStyle;
            }
        }
        newStyle = true;
        return manager.getService(VisualStyleFactory.class).createVisualStyle(getStyleName());
    }

    private void createStyle() {
        setNodeShapeStyle();
        setNodePaintStyle();
        setSelectedNodePaint();
        setNodeBorderPaintStyle();
        setNodeBorderWidth();
        setNodeLabel();
        setNodeLabelColor();

        setEdgeLineTypeStyle();
        setEdgePaintStyle();
        setEdgeWidth();
        setEdgeSourceShape();
        setEdgeTargetShape();
        setEdgeArrowColor();
    }

    protected void setNodeShapeStyle() {
        DiscreteMapping<String, NodeShape> dMapping = (DiscreteMapping<String, NodeShape>) discreteFactory.createVisualMappingFunction(ModelUtils.TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
        dMapping.putMapValue("small molecule", NodeShapeVisualProperty.TRIANGLE);
        dMapping.putMapValue("protein", NodeShapeVisualProperty.ELLIPSE);
        dMapping.putMapValue("gene", NodeShapeVisualProperty.ROUND_RECTANGLE);
        dMapping.putMapValue("dna", BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE"));
        dMapping.putMapValue("rna", NodeShapeVisualProperty.DIAMOND);
        dMapping.putMapValue("complex", NodeShapeVisualProperty.HEXAGON);

        style.addVisualMappingFunction(dMapping);
    }

    protected void setNodePaintStyle() {
        DiscreteMapping<Long, Paint> taxIdToNodeColor = (DiscreteMapping<Long, Paint>) discreteFactory.createVisualMappingFunction(ModelUtils.TAX_ID, Long.class, BasicVisualLexicon.NODE_FILL_COLOR);
        style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(157, 177, 128));
        setNodePaintDiscreteMapping(taxIdToNodeColor);
    }

    protected void setSelectedNodePaint() {
        style.setDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT, new Color(204,0, 51));
    }

    protected void setNodeBorderPaintStyle() {
        DiscreteMapping<Long, Paint> taxIdToNodeBorderColor = (DiscreteMapping<Long, Paint>) discreteFactory.createVisualMappingFunction(ModelUtils.TAX_ID, Long.class, BasicVisualLexicon.NODE_BORDER_PAINT);
        style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, new Color(157, 177, 128));
        setNodePaintDiscreteMapping(taxIdToNodeBorderColor);
    }


    protected void setNodeBorderWidth() {
        style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0d);
    }

    private void setNodeLabel() {
        PassthroughMapping<String, String> nameToLabel = (PassthroughMapping<String, String>) passthroughFactory.createVisualMappingFunction(CyNetwork.NAME, String.class, BasicVisualLexicon.NODE_LABEL);

        style.addVisualMappingFunction(nameToLabel);
    }

    private void setNodeLabelColor() {
        style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, Color.WHITE);
    }

    private void setNodePaintDiscreteMapping(DiscreteMapping<Long, Paint> dMapping) {
        dMapping.putMapValue(9606L, new Color(51, 94, 148)); // Homo Sapiens
        dMapping.putMapValue(4932L, new Color(107, 13, 10)); // Saccharomyces cerevisiae
        dMapping.putMapValue(10090L, new Color(88, 115, 29)); // Mus musculus
        dMapping.putMapValue(3702L, new Color(97, 74, 124)); // Arabidopsis thaliana (Mouse-ear cress)
        dMapping.putMapValue(7227L, new Color(47, 132, 156)); // Drosophila melanogaster
        dMapping.putMapValue(6239L, new Color(202, 115, 47)); // Caenorhabditis elegans
        dMapping.putMapValue(562L, new Color(144, 163, 198)); // Escherichia coli
        dMapping.putMapValue(-2L, new Color(141, 102, 102)); // chemical synthesis

        style.addVisualMappingFunction(dMapping);
    }


    protected void setEdgeLineTypeStyle() {
        DiscreteMapping<String, LineType> dMapping = (DiscreteMapping<String, LineType>) discreteFactory.createVisualMappingFunction("style::shape", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        dMapping.putMapValue("solid", LineTypeVisualProperty.SOLID);
        dMapping.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(dMapping);
    }

    protected abstract void setEdgePaintStyle();

    protected void setEdgeWidth() {
    }

    protected void setEdgeSourceShape() {
    }

    protected void setEdgeTargetShape() {
    }

    private void setEdgeArrowColor() {
        style.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, Color.RED);
    }

    public void registerStyle() {
        vmm.addVisualStyle(style);
    }

    public void applyStyle(CyNetworkView networkView) {
        vmm.setVisualStyle(style, networkView);
        style.apply(networkView);
        networkView.updateView();
    }

    public void applyStyle() {
        vmm.setCurrentVisualStyle(style);
    }

    public void removeStyle() {
        vmm.removeVisualStyle(style);
    }

    public abstract String getStyleName();

}

