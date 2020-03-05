package uk.ac.ebi.intact.intactApp.internal.utils.styles;

import org.cytoscape.event.CyEventHelper;
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
    protected VisualMappingFunctionFactory functionalFactory;

    public IntactStyle(IntactManager manager, String styleName) {
        this.manager = manager;
        vmm = manager.getService(VisualMappingManager.class);
        eventHelper = manager.getService(CyEventHelper.class);
        VisualStyleFactory vsf = manager.getService(VisualStyleFactory.class);
        style = vsf.createVisualStyle(styleName);
        continuousFactory = manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
        discreteFactory = manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        passthroughFactory = manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        functionalFactory = manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=functional)");
        createStyle();
        registerStyle();
    }

    private void createStyle() {
        setNodeShapeStyle();
        setNodePaintStyle();
        setNodeBorderPaintStyle();

        setEdgeLineTypeStyle();
        setEdgePaintStyle();
    }

    protected void setNodeShapeStyle() {
        DiscreteMapping<String, NodeShape> dMapping = (DiscreteMapping) discreteFactory.createVisualMappingFunction(ModelUtils.TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
        dMapping.putMapValue("small molecule", NodeShapeVisualProperty.TRIANGLE);
        dMapping.putMapValue("protein", NodeShapeVisualProperty.ELLIPSE);
        dMapping.putMapValue("gene", NodeShapeVisualProperty.ROUND_RECTANGLE);
        dMapping.putMapValue("dna", BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE"));
        dMapping.putMapValue("rna", NodeShapeVisualProperty.DIAMOND);
        dMapping.putMapValue("complex", NodeShapeVisualProperty.HEXAGON);

        style.addVisualMappingFunction(dMapping);
    }

    protected void setNodePaintStyle() {
        DiscreteMapping<Long, Paint> dMapping = (DiscreteMapping) discreteFactory.createVisualMappingFunction(ModelUtils.TAX_ID, Long.class, BasicVisualLexicon.NODE_FILL_COLOR);
        dMapping.putMapValue(9606L, new Color(51, 94, 148)); // Homo Sapiens
        dMapping.putMapValue(4932L, new Color(107, 13, 10)); // Saccharomyces cerevisiae
        dMapping.putMapValue(10090L, new Color(88, 115, 29)); // Mus musculus Saccharomyces cerevisiae
        dMapping.putMapValue(3702L, new Color(97, 74, 124)); // Arabidopsis thaliana (Mouse-ear cress)
        dMapping.putMapValue(7227L, new Color(47, 132, 156)); // Drosophila melanogaster
        dMapping.putMapValue(6239L, new Color(202, 115, 47)); // Caenorhabditis elegans
        dMapping.putMapValue(562L, new Color(144, 163, 198)); // Escherichia coli
        dMapping.putMapValue(-2L, new Color(141, 102, 102)); // chemical synthesis
        style.setDefaultValue(BasicVisualLexicon.NODE_PAINT, new Color(255, 204, 153));

        style.addVisualMappingFunction(dMapping);
    }

    protected void setNodeBorderPaintStyle() {
        DiscreteMapping<Long, Paint> dMapping = (DiscreteMapping) discreteFactory.createVisualMappingFunction(ModelUtils.TAX_ID, Long.class, BasicVisualLexicon.NODE_BORDER_PAINT);
        dMapping.putMapValue(9606L, new Color(51, 94, 148)); // Homo Sapiens
        dMapping.putMapValue(4932L, new Color(107, 13, 10)); // Saccharomyces cerevisiae
        dMapping.putMapValue(10090L, new Color(88, 115, 29)); // Mus musculus Saccharomyces cerevisiae
        dMapping.putMapValue(3702L, new Color(97, 74, 124)); // Arabidopsis thaliana (Mouse-ear cress)
        dMapping.putMapValue(7227L, new Color(47, 132, 156)); // Drosophila melanogaster
        dMapping.putMapValue(6239L, new Color(202, 115, 47)); // Caenorhabditis elegans
        dMapping.putMapValue(562L, new Color(144, 163, 198)); // Escherichia coli
        dMapping.putMapValue(-2L, new Color(141, 102, 102)); // chemical synthesis
        style.setDefaultValue(BasicVisualLexicon.NODE_PAINT, new Color(255, 204, 153));

        style.addVisualMappingFunction(dMapping);
    }

    protected void setEdgeLineTypeStyle() {
        DiscreteMapping<String, LineType> dMapping = (DiscreteMapping) discreteFactory.createVisualMappingFunction("style::shape", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        dMapping.putMapValue("solid", LineTypeVisualProperty.SOLID);
        dMapping.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(dMapping);
    }

    protected abstract void setEdgePaintStyle();


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

    public String getStyleName() {
        return style.getTitle();
    }

    public void removeStyle() {
        vmm.removeVisualStyle(style);
    }


}

