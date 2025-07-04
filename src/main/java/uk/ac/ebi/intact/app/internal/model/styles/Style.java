package uk.ac.ebi.intact.app.internal.model.styles;

import lombok.Getter;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
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

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NodeFields;
import uk.ac.ebi.intact.app.internal.utils.TimeUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Map;

public abstract class Style {
    public static final Color defaultNodeColor = new Color(157, 177, 128);

    @Getter
    protected VisualStyle style;
    protected Manager manager;
    protected CyEventHelper eventHelper;
    protected VisualMappingManager vmm;
    protected VisualMappingFunctionFactory continuousFactory;
    protected VisualMappingFunctionFactory discreteFactory;
    protected VisualMappingFunctionFactory passthroughFactory;

    private boolean newStyle;
    protected DiscreteMapping<String, NodeShape> nodeTypeToShape;
    protected DiscreteMapping<String, Paint> taxIdToNodeColor;

    private boolean fancy;
    private static PassthroughMapping<String, String> fastLabelsMapping;
    private static PassthroughMapping fancyLabelsMapping;
    private static VisualProperty fancyLabelsProperty;
    private static VisualProperty fancyLabelsPositionProperty;
    private static Object fancyLabelsPositionValue;

    public Style(Manager manager, VisualStyle style) {
        this.manager = manager;
        vmm = manager.utils.getService(VisualMappingManager.class);
        eventHelper = manager.utils.getService(CyEventHelper.class);
        this.style = style;
        newStyle = false;
        loadStyle();

        continuousFactory = manager.utils.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
        discreteFactory = manager.utils.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        passthroughFactory = manager.utils.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
    }

    public Style(Manager manager) {
        this.manager = manager;
        vmm = manager.utils.getService(VisualMappingManager.class);
        eventHelper = manager.utils.getService(CyEventHelper.class);
        style = getOrCreateStyle();
        continuousFactory = manager.utils.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
        discreteFactory = manager.utils.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        passthroughFactory = manager.utils.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        if (newStyle) {
            createStyle();
            registerStyle();
        } else {
            loadStyle();
        }
    }

    private VisualStyle getOrCreateStyle() {
        for (VisualStyle createdStyle : vmm.getAllVisualStyles()) {
            if (createdStyle.getTitle().equals(getStyleName())) {
                newStyle = false;
                return createdStyle;
            }
        }
        newStyle = true;
        return manager.utils.getService(VisualStyleFactory.class).createVisualStyle(getStyleName());
    }

    private void createStyle() {
        setNetworkBackground();

        setNodeShapeStyle();
        setNodePaintStyle();
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
        setEdgeLabel();
    }

    @SuppressWarnings("unchecked")
    protected void loadStyle() {
        setNodeLabel();
        setEdgeLabel();
        nodeTypeToShape = (DiscreteMapping<String, NodeShape>) style.getVisualMappingFunction(BasicVisualLexicon.NODE_SHAPE);
        taxIdToNodeColor = (DiscreteMapping<String, Paint>) style.getVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
    }

    protected void setNetworkBackground() {
        style.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, new Color(251, 251, 251));
    }

    protected void setNodeShapeStyle() {
        nodeTypeToShape = (DiscreteMapping<String, NodeShape>) discreteFactory.createVisualMappingFunction(NodeFields.TYPE.VALUE.toString(), String.class, BasicVisualLexicon.NODE_SHAPE);
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

    public void setNodePaintStyle() {
        taxIdToNodeColor = (DiscreteMapping<String, Paint>) discreteFactory.createVisualMappingFunction(NodeFields.TAX_ID.toString(), String.class, BasicVisualLexicon.NODE_FILL_COLOR);
        taxIdToNodeColor.putAll(StyleMapper.speciesColors);
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
            VisualLexicon lex = manager.utils.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
            fastLabelsMapping = (PassthroughMapping<String, String>) passthroughFactory.createVisualMappingFunction(NodeFields.NAME.toString(), String.class, BasicVisualLexicon.NODE_LABEL);
            fancyLabelsProperty = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_3");
            fancyLabelsMapping = (PassthroughMapping) passthroughFactory.createVisualMappingFunction(NodeFields.ELABEL_STYLE.toString(), String.class, fancyLabelsProperty);

            fancyLabelsPositionProperty = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_POSITION_3");
            fancyLabelsPositionValue = fancyLabelsPositionProperty.parseSerializableString("C,C,c,0.00,-4.00");
        }
        if (manager.utils.haveEnhancedGraphics()) {
            style.addVisualMappingFunction(fancyLabelsMapping);
            style.setDefaultValue(fancyLabelsPositionProperty, fancyLabelsPositionValue);
            style.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
            fancy = true;
        } else {
            style.addVisualMappingFunction(fastLabelsMapping);
            fancy = false;
        }

    }

    public void toggleFancy() {
        if (!fancy && manager.utils.haveEnhancedGraphics()) {
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

    public void setFancy(boolean fancy) {
        if (this.fancy != fancy) {
            toggleFancy();
        }
    }

    private void setNodeLabelColor() {
        style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, Color.BLACK);
    }

    private void addMissingNodePaint(DiscreteMapping<String, Paint> taxIdToPaint) {
        new Thread(() -> {
            while (StyleMapper.speciesNotReady()) {
                TimeUtils.sleep(100);
            }
            taxIdToPaint.putAll(StyleMapper.speciesColors);
        }).start();
    }

    public void updateTaxIdToNodePaintMapping(Map<String, Paint> toPut) {
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

    public String getStyleName(){
        return getStyleViewType().styleName;
    }

    public abstract NetworkView.Type getStyleViewType();

    @Override
    public String toString() {
        return getStyleName();
    }

    protected void setEdgeLabel() {
        DiscreteMapping<Boolean, String> edgeLabelMapping =
                (DiscreteMapping<Boolean, String>) discreteFactory.createVisualMappingFunction(
                        EdgeFields.IS_NEGATIVE_INTERACTION.toString(),
                        Boolean.class,
                        BasicVisualLexicon.EDGE_LABEL
                );

        edgeLabelMapping.putMapValue(false, "");
        edgeLabelMapping.putMapValue(true, "X");

        VisualLexicon lex = manager.utils.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
        VisualProperty labelPosition = lex.lookup(CyEdge.class, "EDGE_LABEL_AUTOROTATE");
        style.addVisualMappingFunction(edgeLabelMapping);
        style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, Color.RED);
        style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, 30);
        style.setDefaultValue(labelPosition, true);
    }
}