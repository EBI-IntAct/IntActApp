package uk.ac.ebi.intact.intactApp.internal.model.styles;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.OLSMapper;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TimeUtils;

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
        DiscreteMapping<String, NodeShape> nodeTypeToShape = (DiscreteMapping<String, NodeShape>) discreteFactory.createVisualMappingFunction(ModelUtils.TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
        nodeTypeToShape.putAll(OLSMapper.nodeTypeToShape);

        style.addVisualMappingFunction(nodeTypeToShape);
        addMissingNodeShape(nodeTypeToShape);
    }

    private void addMissingNodeShape(DiscreteMapping<String, NodeShape> interactorTypeToShape) {
        new Thread(() -> {
            OLSMapper.initializeNodeTypeToShape();
            while (OLSMapper.nodeTypesNotReady()){
                TimeUtils.sleep(100);
            }
            interactorTypeToShape.putAll(OLSMapper.nodeTypeToShape);
        }).start();
    }

    protected void setSelectedNodePaint() {
        style.setDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT, new Color(204, 0, 51));
    }

    protected void setNodePaintStyle() {
        DiscreteMapping<Long, Paint> taxIdToNodeColor = (DiscreteMapping<Long, Paint>) discreteFactory.createVisualMappingFunction(ModelUtils.TAX_ID, Long.class, BasicVisualLexicon.NODE_FILL_COLOR);
        style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(157, 177, 128));
        setNodePaintDiscreteMapping(taxIdToNodeColor);
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

    private void setNodePaintDiscreteMapping(DiscreteMapping<Long, Paint> taxIdToPaint) {
        taxIdToPaint.putAll(OLSMapper.taxIdToPaint);
        style.addVisualMappingFunction(taxIdToPaint);
        addMissingNodePaint(taxIdToPaint);
    }

    private void addMissingNodePaint(DiscreteMapping<Long, Paint> taxIdToPaint) {
        new Thread(() -> {
            OLSMapper.initializeTaxIdToPaint();
            while (OLSMapper.speciesNotReady()){
                TimeUtils.sleep(100);
            }
            taxIdToPaint.putAll(OLSMapper.taxIdToPaint);
        }).start();
    }



    protected void setEdgeLineTypeStyle() {
        DiscreteMapping<String, LineType> dMapping = (DiscreteMapping<String, LineType>) discreteFactory.createVisualMappingFunction("style::shape", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        dMapping.putMapValue("solid", LineTypeVisualProperty.SOLID);
        dMapping.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(dMapping);
    }

    protected abstract void setEdgePaintStyle();

    protected void setEdgeWidth() {
        style.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 2.0);
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

