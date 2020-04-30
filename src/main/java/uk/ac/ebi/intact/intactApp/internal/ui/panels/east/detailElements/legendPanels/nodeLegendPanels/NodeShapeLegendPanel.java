package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.nodeLegendPanels;

import org.apache.commons.lang3.StringUtils;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.ui.components.legend.shapes.*;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.AbstractLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.intactApp.internal.utils.TimeUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.AbstractDetailPanel.backgroundColor;

public class NodeShapeLegendPanel extends AbstractLegendPanel {
    private final EasyGBC layoutHelper = new EasyGBC();
    private final Map<String, JPanel> nodeShapesLines = new HashMap<>();


    public NodeShapeLegendPanel(IntactManager manager, IntactNetwork currentINetwork, IntactNetworkView currentIView) {
        super("<html>Node Shape <em>~ Interactor Type</em></html>", manager, currentINetwork, currentIView);
        for (String nodeType : StyleMapper.originalNodeTypeToShape.keySet()) {
            JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            linePanel.setBackground(backgroundColor);
            NodeShape nodeShape = StyleMapper.nodeTypeToShape.get(nodeType);

            JComponent legend;
            Color legendColor = Color.LIGHT_GRAY;
            int legendSize = 30;

            if (nodeShape == NodeShapeVisualProperty.TRIANGLE) {
                legend = new Triangle(legendSize, legendSize, legendColor);
            } else if (nodeShape == NodeShapeVisualProperty.ROUND_RECTANGLE) {
                legend = new RoundedRectangle(legendSize, legendSize, legendColor);
            } else if (nodeShape == BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE")) {
                legend = new Vee(legendSize, legendSize, legendColor);
            } else if (nodeShape == NodeShapeVisualProperty.DIAMOND) {
                legend = new Diamond(legendSize, legendSize, legendColor);
            } else if (nodeShape == NodeShapeVisualProperty.HEXAGON) {
                legend = new Hexagon(legendSize, legendSize, legendColor);
            } else if (nodeShape == NodeShapeVisualProperty.OCTAGON) {
                legend = new Octagon(legendSize, legendSize, legendColor);
            } else {
                legend = new Ball(legendColor, legendSize);
            }
            linePanel.add(legend);
            JLabel label = new JLabel(StringUtils.capitalize(nodeType));
            label.setBorder(new EmptyBorder(0, 4, 0, 0));
            linePanel.add(label);
            content.add(linePanel, layoutHelper.down().expandHoriz());
            nodeShapesLines.put(nodeType, linePanel);
        }
    }

    @Override
    public void filterCurrentLegend() {
        new Thread(() -> {
            while (!currentINetwork.isStyleCompleted())
                TimeUtils.sleep(100);

            Set<String> networkInteractorTypes = currentINetwork.getInteractorTypes();

            for (String nodeType : nodeShapesLines.keySet()) {
                nodeShapesLines.get(nodeType).setVisible(
                        networkInteractorTypes.contains(nodeType) ||
                                (StyleMapper.nodeTypeToParent.containsKey(nodeType) && !Collections.disjoint(networkInteractorTypes, StyleMapper.nodeTypeToParent.get(nodeType)))
                );
            }

        }).start();
    }
}
