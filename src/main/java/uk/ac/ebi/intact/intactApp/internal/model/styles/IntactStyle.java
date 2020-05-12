package uk.ac.ebi.intact.intactApp.internal.model.styles;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TimeUtils;

import java.awt.*;
import java.util.Map;

public abstract class IntactStyle {
    public static final Color defaultNodeColor = new Color(157, 177, 128);
    protected VisualStyle style;
    protected IntactManager manager;
    protected CyEventHelper eventHelper;
    protected VisualMappingManager vmm;
    protected VisualMappingFunctionFactory continuousFactory;
    protected VisualMappingFunctionFactory discreteFactory;
    protected VisualMappingFunctionFactory passthroughFactory;

    private boolean newStyle;
    protected DiscreteMapping<String, NodeShape> nodeTypeToShape;
    protected DiscreteMapping<Long, Paint> taxIdToNodeColor;

    private boolean fancy;
    private static PassthroughMapping<String, String> fastLabelsMapping;
    private static PassthroughMapping fancyLabelsMapping;
    private static VisualProperty fancyLabelsProperty;
    private static VisualProperty fancyLabelsPositionProperty;
    private static Object fancyLabelsPositionValue;

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
        setNetworkBackground();

        setNodeShapeStyle();
        setNodePaintStyle();
        setSelectedNodePaint();
        setNodeBorderPaintStyle();
        setNodeBorderWidth();
        setNodeLabelColor();
        setNodeLabel();

        setEdgeLineTypeStyle();
        setEdgePaintStyle();
        setEdgeWidth();
        setEdgeSourceShape();
        setEdgeTargetShape();
        setEdgeArrowColor();
    }

    protected void setNetworkBackground() {
        style.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, new Color(251, 251, 251));
    }

    protected void setNodeShapeStyle() {
        nodeTypeToShape = (DiscreteMapping<String, NodeShape>) discreteFactory.createVisualMappingFunction(ModelUtils.TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
        nodeTypeToShape.putAll(StyleMapper.nodeTypeToShape);

        style.addVisualMappingFunction(nodeTypeToShape);
        addMissingNodeShape();
    }

    public void updateNodeTypeToShapeMapping(Map<String, NodeShape> toPut) {
        nodeTypeToShape.putAll(toPut);
    }

    private void addMissingNodeShape() {
        new Thread(() -> {
            while (StyleMapper.nodeTypesNotReady()) {
                TimeUtils.sleep(100);
            }
            updateNodeTypeToShapeMapping(StyleMapper.nodeTypeToShape);
        }).start();
    }

    protected void setSelectedNodePaint() {
//        style.setDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT, new Color(204, 0, 51));
    }

    public void setNodePaintStyle() {
        taxIdToNodeColor = (DiscreteMapping<Long, Paint>) discreteFactory.createVisualMappingFunction(ModelUtils.TAX_ID, Long.class, BasicVisualLexicon.NODE_FILL_COLOR);
        taxIdToNodeColor.putAll(StyleMapper.taxIdToPaint);
        style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, defaultNodeColor);
        style.addVisualMappingFunction(taxIdToNodeColor);
        addMissingNodePaint(taxIdToNodeColor);
    }

    protected void setNodeBorderPaintStyle() {
    }


    protected void setNodeBorderWidth() {
        style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0d);
    }

    private void setNodeLabel() {
        if (fastLabelsMapping == null) {
            VisualLexicon lex = manager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
            fastLabelsMapping = (PassthroughMapping<String, String>) passthroughFactory.createVisualMappingFunction(CyNetwork.NAME, String.class, BasicVisualLexicon.NODE_LABEL);
            if (manager.haveEnhancedGraphics()) {

                fancyLabelsProperty = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_3");
                fancyLabelsMapping = (PassthroughMapping) passthroughFactory.createVisualMappingFunction(ModelUtils.ELABEL_STYLE, String.class, fancyLabelsProperty);
                style.addVisualMappingFunction(fancyLabelsMapping);

                fancyLabelsPositionProperty = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_POSITION_3");
                fancyLabelsPositionValue = fancyLabelsPositionProperty.parseSerializableString("C,C,c,0.00,-4.00");
                style.setDefaultValue(fancyLabelsPositionProperty, fancyLabelsPositionValue);
                style.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);

                fancy = true;
            } else {
                style.addVisualMappingFunction(fastLabelsMapping);
                fancy = false;
            }
        } else {
            if (manager.haveEnhancedGraphics()) {
                style.addVisualMappingFunction(fancyLabelsMapping);
                style.setDefaultValue(fancyLabelsPositionProperty, fancyLabelsPositionValue);
                style.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
                fancy = true;
            } else {
                style.addVisualMappingFunction(fastLabelsMapping);
                fancy = false;
            }
        }
    }

    public void toggleFancy() {
        if (!fancy && manager.haveEnhancedGraphics()) {
            if (fancyLabelsMapping == null)
                setNodeLabel();
            style.addVisualMappingFunction(fancyLabelsMapping);
            style.setDefaultValue(fancyLabelsPositionProperty, fancyLabelsPositionValue);
            style.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
            fancy = true;
        } else {
            style.removeVisualMappingFunction(fancyLabelsProperty);
            style.removeVisualMappingFunction(fancyLabelsPositionProperty);
            style.addVisualMappingFunction(fastLabelsMapping);
            fancy = false;
        }
    }

    private void setNodeLabelColor() {
        style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, Color.BLACK);
    }

    private void addMissingNodePaint(DiscreteMapping<Long, Paint> taxIdToPaint) {
        new Thread(() -> {
            while (StyleMapper.speciesNotReady()) {
                TimeUtils.sleep(100);
            }
            taxIdToPaint.putAll(StyleMapper.taxIdToPaint);
        }).start();
    }

    public void updateTaxIdToNodePaintMapping(Map<Long, Paint> toPut) {
        if (taxIdToNodeColor != null) {
            taxIdToNodeColor.putAll(toPut);
        }
    }

    protected void setEdgeLineTypeStyle() {
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
    public abstract IntactNetworkView.Type getStyleViewType();

}

