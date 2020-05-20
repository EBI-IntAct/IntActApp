package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.edge.elements;

import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.model.styles.CollapsedIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.ui.components.diagrams.NodeDiagram;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeBasics;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeFeatures;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;
import java.util.*;

public class EdgeParticipants extends AbstractEdgeElement {
    private JPanel sourcePanel;
    private JPanel targetPanel;
    private static final Color nodePanelBg = new Color(229, 229, 229);
    private static final Color nodePanelBorder = new Color(186, 186, 186);
    private final List<NodeFeatures> participantSummaries = new ArrayList<>();
    private NodeDiagram sourceDiagram;
    private NodeDiagram targetDiagram;


    public EdgeParticipants(IntactEdge iEdge, OpenBrowser openBrowser) {
        super(null, iEdge, openBrowser);
        fillContent();
    }

    @Override
    protected void fillCollapsedEdgeContent(IntactCollapsedEdge edge) {
        createPanel(edge);
        Map<IntactNode, List<Feature>> features = edge.getFeatures();
        for (IntactNode iNode : List.of(edge.source, edge.target)) {
            JPanel nodePanel = iNode == edge.source ? sourcePanel : targetPanel;
            EasyGBC layoutHelper = new EasyGBC();
            nodePanel.setBackground(nodePanelBg);
            nodePanel.setOpaque(true);

            NodeBasics nodeBasics = new NodeBasics(iNode, openBrowser);
            nodeBasics.setBackground(nodePanelBg);
            nodePanel.add(nodeBasics, layoutHelper.down().anchor("north").expandHoriz());

            NodeFeatures nodeFeatures = new NodeFeatures(iNode, features.get(iNode), openBrowser, true, false, nodePanelBg);
            nodePanel.add(nodeFeatures, layoutHelper.down().anchor("north").expandHoriz());
            participantSummaries.add(nodeFeatures);

            nodePanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
        }

        int thickness = edge.edges.size() + 2;
        thickness = Integer.min(thickness, 25);
        content.add(new EdgeDiagram(CollapsedIntactStyle.getColor(edge.miScore), thickness, false));
    }

    @Override
    protected void fillEvidenceEdgeContent(IntactEvidenceEdge edge) {
        createPanel(edge);
        Map<IntactNode, List<Feature>> features = edge.getFeatures();
        for (IntactNode iNode : List.of(edge.source, edge.target)) {
            JPanel nodePanel = iNode == edge.source ? sourcePanel : targetPanel;
            EasyGBC layoutHelper = new EasyGBC();
            nodePanel.setBackground(nodePanelBg);
            nodePanel.setOpaque(true);

            NodeBasics nodeBasics = new NodeBasics(iNode, openBrowser);
            nodeBasics.setBackground(nodePanelBg);
            nodePanel.add(nodeBasics, layoutHelper.down().anchor("north").expandHoriz());

            String biologicalRole = StringUtils.capitalize(iNode == edge.source ? edge.sourceBiologicalRole : edge.targetBiologicalRole);
            JLabel biologicalRoleLabel = new JLabel("Biological role : " + biologicalRole);
            biologicalRoleLabel.setBackground(nodePanelBg);
            biologicalRoleLabel.setOpaque(true);
            biologicalRoleLabel.setAlignmentY(TOP_ALIGNMENT);
            nodePanel.add(biologicalRoleLabel, layoutHelper.down().anchor("north").expandHoriz());

            NodeFeatures nodeFeatures = new NodeFeatures(iNode, features.get(iNode), openBrowser, false, false, nodePanelBg);
            nodePanel.add(nodeFeatures, layoutHelper.down().anchor("north").expandHoriz());

            nodePanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
        }

        content.add(new EdgeDiagram(StyleMapper.edgeTypeToPaint.get(edge.type), 4, edge.expansionType != null));
    }

    private static final Map<IntactNode, NodeDiagramInfo> nodeDiagramInfos = new HashMap<>();

    private static class NodeDiagramInfo {
        int width;
        List<NodeDiagram> nodeDiagrams = new ArrayList<>();

        public NodeDiagramInfo(NodeDiagram nodeDiagram) {
            this.width = nodeDiagram.getPreferredSize().width;
            nodeDiagrams.add(nodeDiagram);
        }
    }

    public static void homogenizeNodeDiagramWidth() {
        Optional<Integer> max = nodeDiagramInfos.values().stream().map(nodeDiagramInfo -> nodeDiagramInfo.width).max(Integer::compareTo);
        if (max.isPresent()) {
            int maxWidth = max.get();
            for (NodeDiagramInfo nodeDiagramInfo : nodeDiagramInfos.values()) {
                for (NodeDiagram nodeDiagram: nodeDiagramInfo.nodeDiagrams) {
                    Dimension preferredSize = nodeDiagram.getPreferredSize();
                    preferredSize.width = maxWidth;
                    nodeDiagram.setPreferredSize(preferredSize);
                }
            }
        }
    }

    private void createPanel(IntactEdge edge) {
        content.setLayout(new OverlayLayout(content));
        content.setOpaque(false);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        JPanel participantsPanel = new JPanel(new GridBagLayout());
        participantsPanel.setOpaque(false);
        EasyGBC c = new EasyGBC();
        EasyGBC d = new EasyGBC();
        mainPanel.add(participantsPanel, d.right().expandBoth());

        MatteBorder outsideBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, nodePanelBorder);
        MatteBorder insideBorder = BorderFactory.createMatteBorder(4, 4, 4, 4, nodePanelBg);
        CompoundBorder border = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);


        Map<IntactNode, List<Feature>> featuresMap = edge.getFeatures();
        {
            sourcePanel = new JPanel(new GridBagLayout());
            sourcePanel.setBackground(nodePanelBg);
            sourcePanel.setBorder(border);
            sourceDiagram = new NodeDiagram(edge.source, featuresMap.get(edge.source));
            System.out.println(System.identityHashCode(sourceDiagram));
            sourceDiagram.setBorder(new EmptyBorder(0, 4, 0, 4));
            sourceDiagram.setOpaque(false);
            participantsPanel.add(sourceDiagram, c.noExpand());
            participantsPanel.add(sourcePanel, c.right().expandHoriz());
        }
        participantsPanel.add(Box.createVerticalStrut(5), c.down().noExpand());
        participantsPanel.add(Box.createVerticalStrut(5), c.right().expandHoriz());
        {
            targetPanel = new JPanel(new GridBagLayout());
            targetPanel.setBackground(nodePanelBg);
            targetPanel.setBorder(border);
            targetDiagram = new NodeDiagram(edge.target, featuresMap.get(edge.target));
            System.out.println(System.identityHashCode(sourceDiagram));
            targetDiagram.setBorder(new EmptyBorder(0, 4, 0, 4));
            targetDiagram.setOpaque(false);
            participantsPanel.add(targetDiagram, c.down().noExpand());
            participantsPanel.add(targetPanel, c.right().expandHoriz());
        }

        for (NodeDiagram nodeDiagram : List.of(sourceDiagram, targetDiagram)) {
            if (!nodeDiagramInfos.containsKey(nodeDiagram.node)) {
                nodeDiagramInfos.put(nodeDiagram.node, new NodeDiagramInfo(nodeDiagram));
            } else {
                nodeDiagramInfos.get(nodeDiagram.node).nodeDiagrams.add(nodeDiagram);
            }
        }
        content.add(mainPanel);
    }

    public void delete() {
        for (NodeFeatures participantSummary : participantSummaries) {
            participantSummary.deleteEdgeSelectionCheckboxes();
        }
        for (NodeDiagram nodeDiagram : List.of(sourceDiagram, targetDiagram)) {
            NodeDiagramInfo nodeDiagramInfo = nodeDiagramInfos.get(nodeDiagram.node);
            nodeDiagramInfo.nodeDiagrams.remove(nodeDiagram);
            if (nodeDiagramInfo.nodeDiagrams.isEmpty()) {
                nodeDiagramInfos.remove(nodeDiagram.node);
            }
        }
    }


    private class EdgeDiagram extends JComponent {
        Paint color;
        boolean dashed;
        int thickness;

        public EdgeDiagram(Paint paint, int thickness, boolean dashed) {
            this.color = paint;
            this.dashed = dashed;
            this.thickness = thickness;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(color);
            if (dashed) {
                g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[]{6.0f, 5.0f}, 0));
            } else {
                g2.setStroke(new BasicStroke(thickness));
            }

            Rectangle sourceBounds = sourceDiagram.getBounds();
            Rectangle targetBounds = targetDiagram.getBounds();
            int centerX = (int) sourceBounds.getCenterX();
            g2.drawLine(centerX, (int) sourceBounds.getCenterY() + 15, centerX, (int) targetBounds.getCenterY() - 15);
        }
    }
}
