package uk.ac.ebi.intact.intactApp.internal.utils;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.*;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.*;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ViewUtils {
    public static String STYLE_NAME = "STRING style";
    public static String STYLE_NAME_NAMESPACES = "STRING style v1.5";
    public static String STYLE_NAME_ORG = "Organism STRING style";
    public static String STYLE_NAME_ORG_NAMESPACES = "Organism STRING style v1.5";
    public static String STYLE_ORG = "Organism ";

    // Our chart strings
    static String PIE_CHART = "piechart: attributelist=\"enrichmentTermsIntegers\" showlabels=\"false\" colorlist=\"";
    static String CIRCOS_CHART = "circoschart: firstarc=1.0 arcwidth=0.4 attributelist=\"enrichmentTermsIntegers\" showlabels=\"false\" colorlist=\"";
    static String CIRCOS_CHART2 = "circoschart: borderwidth=0 firstarc=1.0 arcwidth=0.4 attributelist=\"enrichmentTermsIntegers\" showlabels=\"false\" colorlist=\"";

    public static CyNetworkView styleNetwork(IntactManager manager, CyNetwork network,
                                             CyNetworkView netView) {
        boolean useStitch = false;
        if (network.getDefaultNodeTable().getColumn(ModelUtils.TYPE) != null)
            useStitch = true;
//        VisualStyle stringStyle = createStyle(manager, network, useStitch);
//
//        updateColorMap(manager, stringStyle, network);
//        updateEnhancedLabels(manager, stringStyle, network, manager.showEnhancedLabels());
//        updateGlassBallEffect(manager, stringStyle, network, manager.showGlassBallEffect());

//        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
//        vmm.setCurrentVisualStyle(stringStyle);

        if (netView != null) {
//            vmm.setVisualStyle(stringStyle, netView);
            manager.getService(CyNetworkViewManager.class).addNetworkView(netView);
            manager.getService(CyApplicationManager.class).setCurrentNetworkView(netView);

        }

        return netView;
    }

    public static void reapplyStyle(IntactManager manager, CyNetworkView view) {
        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        VisualStyle style = vmm.getVisualStyle(view);
        style.apply(view);
    }

    public static void updateNodeStyle(IntactManager manager,
                                       CyNetworkView view, List<CyNode> nodes) {
        // manager.flushEvents();
        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        VisualStyle style = vmm.getVisualStyle(view);
        for (CyNode node : nodes) {
            if (view.getNodeView(node) != null)
                style.apply(view.getModel().getRow(node), view.getNodeView(node));
        }
        // style.apply(view);
    }

    public static void updateEdgeStyle(IntactManager manager, CyNetworkView view, List<CyEdge> edges) {
        // manager.flushEvents();
        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        VisualStyle style = vmm.getVisualStyle(view);
        for (CyEdge edge : edges) {
            if (view.getEdgeView(edge) != null)
                style.apply(view.getModel().getRow(edge), view.getEdgeView(edge));
        }
        // style.apply(view);
    }

    public static VisualStyle createStyle(IntactManager manager, CyNetwork network, boolean useStitch) {
        String styleName = getStyleName(manager, network);

        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        for (VisualStyle style : vmm.getAllVisualStyles()) {
            if (style.getTitle().equals(styleName)) {
                return style;
            }
        }

        VisualStyleFactory vsf = manager.getService(VisualStyleFactory.class);

        VisualStyle stringStyle = vsf.createVisualStyle(vmm.getCurrentVisualStyle());
        stringStyle.setTitle(styleName);

        // Lock node width and height
        for (VisualPropertyDependency<?> vpd : stringStyle.getAllVisualPropertyDependencies()) {
            if (vpd.getIdString().equals("nodeSizeLocked"))
                vpd.setDependency(false);
        }

        // Get all of the factories we'll need
        VisualMappingFunctionFactory continuousFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
        VisualMappingFunctionFactory discreteFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        VisualMappingFunctionFactory passthroughFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");

        {
            DiscreteMapping<String, NodeShape> dMapping =
                    (DiscreteMapping) discreteFactory.createVisualMappingFunction(ModelUtils.TYPE, String.class,
                            BasicVisualLexicon.NODE_SHAPE);
            dMapping.putMapValue("small molecule", NodeShapeVisualProperty.TRIANGLE);
            dMapping.putMapValue("protein", NodeShapeVisualProperty.ELLIPSE);
            dMapping.putMapValue("gene", NodeShapeVisualProperty.ROUND_RECTANGLE);
            dMapping.putMapValue("dna", BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE"));
            dMapping.putMapValue("rna", NodeShapeVisualProperty.DIAMOND);
            dMapping.putMapValue("complex", NodeShapeVisualProperty.HEXAGON);

            stringStyle.addVisualMappingFunction(dMapping);
        }


        {
            DiscreteMapping<String, Color> dMapping =
                    (DiscreteMapping) discreteFactory.createVisualMappingFunction(CyEdge.INTERACTION,
                            String.class,
                            BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
            dMapping.putMapValue("physical association", Color.decode("99CC00"));
            dMapping.putMapValue("association", Color.decode("9999FF"));
            dMapping.putMapValue("direct interaction", Color.decode("FFA500"));
            dMapping.putMapValue("colocalization", Color.decode("FFDE3E"));
            dMapping.putMapValue("phosphorylation", Color.decode("990000"));
            dMapping.putMapValue("dephosphorylation", Color.decode("999900"));
            stringStyle.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, Color.decode("999999"));
            stringStyle.addVisualMappingFunction(dMapping);
        }

        vmm.addVisualStyle(stringStyle);
        return stringStyle;
    }

    public static void updateChemVizPassthrough(IntactManager manager, CyNetworkView view, boolean show) {
        VisualStyle stringStyle = getStyle(manager, view);

        VisualMappingFunctionFactory passthroughFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        VisualLexicon lex = manager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();

        if (show && manager.haveChemViz()) {
            VisualProperty customGraphics = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_2");
            PassthroughMapping pMapping =
                    (PassthroughMapping) passthroughFactory.createVisualMappingFunction(ModelUtils.CV_STYLE,
                            String.class, customGraphics);
            stringStyle.addVisualMappingFunction(pMapping);
        } else {
            stringStyle
                    .removeVisualMappingFunction(lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_2"));
        }
    }

    public static void updateEnhancedLabels(IntactManager manager, VisualStyle stringStyle,
                                            CyNetwork net, boolean show) {

        boolean useStitch = false;
        if (net.getDefaultNodeTable().getColumn(ModelUtils.TYPE) != null)
            useStitch = true;

        VisualMappingFunctionFactory discreteFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        VisualMappingFunctionFactory passthroughFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        VisualLexicon lex = manager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
        // Set up the passthrough mapping for the label
        if (show && manager.haveEnhancedGraphics()) {
            {
                VisualProperty customGraphics = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_3");
                PassthroughMapping pMapping =
                        (PassthroughMapping) passthroughFactory.createVisualMappingFunction(ModelUtils.ELABEL_STYLE,
                                String.class, customGraphics);
                stringStyle.addVisualMappingFunction(pMapping);
            }

            // Set up our labels to be in the upper right quadrant
            {
                VisualProperty customGraphicsP = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_POSITION_3");
                Object upperRight = customGraphicsP.parseSerializableString("NE,C,c,0.00,0.00");
                stringStyle.setDefaultValue(customGraphicsP, upperRight);
                if (useStitch) {
                    Object top = customGraphicsP.parseSerializableString("N,C,c,0.00,-5.00");
                    DiscreteMapping<String, Object> dMapping =
                            (DiscreteMapping) discreteFactory.createVisualMappingFunction(ModelUtils.TYPE, String.class,
                                    customGraphicsP);
                    dMapping.putMapValue("compound", top);
                    dMapping.putMapValue("protein", upperRight);
                    stringStyle.addVisualMappingFunction(dMapping);
                }
            }

            {
                stringStyle.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
            }
        } else {
            stringStyle
                    .removeVisualMappingFunction(lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_3"));
            stringStyle.removeVisualMappingFunction(
                    lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_POSITION_3"));

            {
                PassthroughMapping pMapping = (PassthroughMapping) passthroughFactory
                        .createVisualMappingFunction(ModelUtils.DISPLAY, String.class,
                                BasicVisualLexicon.NODE_LABEL);
                stringStyle.addVisualMappingFunction(pMapping);
            }

        }
    }





    public static void highlight(IntactManager manager, CyNetworkView view, List<CyNode> nodes) {
        CyNetwork net = view.getModel();

        List<CyEdge> edgeList = new ArrayList<>();
        List<CyNode> nodeList = new ArrayList<>();
        for (CyNode node : nodes) {
            edgeList.addAll(net.getAdjacentEdgeList(node, CyEdge.Type.ANY));
            nodeList.addAll(net.getNeighborList(node, CyEdge.Type.ANY));
        }


        VisualLexicon lex = manager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
        VisualProperty customGraphics1 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
        VisualProperty customGraphics2 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_2");
        VisualProperty customGraphics3 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_3");

        CyCustomGraphics cg = new EmptyCustomGraphics();

        // Override our current style through overrides
        for (View<CyNode> nv : view.getNodeViews()) {
            if (nodeList.contains(nv.getModel()) || nodes.contains(nv.getModel())) {
                nv.setLockedValue(BasicVisualLexicon.NODE_TRANSPARENCY, 255);
            } else {
                nv.setLockedValue(customGraphics1, cg);
                nv.setLockedValue(customGraphics2, cg);
                nv.setLockedValue(customGraphics3, cg);
                nv.setLockedValue(BasicVisualLexicon.NODE_TRANSPARENCY, 20);
            }
        }
        for (View<CyEdge> ev : view.getEdgeViews()) {
            if (edgeList.contains(ev.getModel())) {
                ev.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 255);
            } else {
                ev.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 20);
            }
        }
    }

    public static void clearHighlight(IntactManager manager, CyNetworkView view) {
        if (view == null) return;

        VisualLexicon lex = manager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
        VisualProperty customGraphics1 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
        VisualProperty customGraphics2 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_2");
        VisualProperty customGraphics3 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_3");

        for (View<CyNode> nv : view.getNodeViews()) {
            nv.clearValueLock(customGraphics1);
            nv.clearValueLock(customGraphics2);
            nv.clearValueLock(customGraphics3);
            nv.clearValueLock(BasicVisualLexicon.NODE_TRANSPARENCY);
        }

        for (View<CyEdge> ev : view.getEdgeViews()) {
            ev.clearValueLock(BasicVisualLexicon.EDGE_TRANSPARENCY);
        }
    }




    public static VisualStyle getStyle(IntactManager manager, CyNetworkView view) {
        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        VisualStyle style = null;
        if (view != null)
            style = vmm.getVisualStyle(view);
        else {
            String styleName = getStyleName(manager, view.getModel());
            for (VisualStyle s : vmm.getAllVisualStyles()) {
                if (s.getTitle().equals(styleName)) {
                    style = s;
                    break;
                }
            }
        }

        return style;
    }

    private static String getStyleName(IntactManager manager, CyNetwork network) {
        String networkName = manager.getNetworkName(network);
        String styleName = STYLE_NAME_NAMESPACES;
        if (networkName.startsWith("String Network")) {
            String[] parts = networkName.split("_");
            if (parts.length == 1) {
                String[] parts2 = networkName.split(" - ");
                if (parts2.length == 2)
                    styleName = styleName + " - " + parts2[1];
            } else if (parts.length == 2)
                styleName = styleName + "_" + parts[1];
        }
        return styleName;
    }
}
