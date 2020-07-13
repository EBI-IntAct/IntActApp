package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.elements;

import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.model.styles.SummaryStyle;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.ui.components.diagrams.NodeDiagram;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.NodeBasics;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.NodeFeatures;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.app.internal.ui.utils.LinkUtils;
import uk.ac.ebi.intact.app.internal.utils.TimeUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class EdgeParticipants extends AbstractEdgeElement {
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    private JPanel sourcePanel;
    private JPanel targetPanel;
    private static final Color nodePanelBg = new Color(229, 229, 229);
    private static final Color nodePanelBorder = new Color(186, 186, 186);
    private final List<NodeFeatures> participantSummaries = new ArrayList<>();
    private NodeDiagram sourceDiagram;
    private NodeDiagram targetDiagram;


    public EdgeParticipants(Edge edge, OpenBrowser openBrowser) {
        super(null, edge, openBrowser);
        executor.execute(this::fillContent);
    }

    @Override
    protected void fillSummaryEdgeContent(SummaryEdge edge) {
        createPanel(edge);
        Map<Node, List<Feature>> features = edge.getFeatures();
        for (Node node : List.of(edge.source, edge.target)) {
            JPanel nodePanel = node == edge.source ? sourcePanel : targetPanel;
            EasyGBC layoutHelper = new EasyGBC();
            nodePanel.setBackground(nodePanelBg);
            nodePanel.setOpaque(true);

            NodeBasics nodeBasics = new NodeBasics(node, openBrowser);
            nodeBasics.setBackground(nodePanelBg);
            nodePanel.add(nodeBasics, layoutHelper.down().anchor("north").expandHoriz());

            NodeFeatures nodeFeatures = new NodeFeatures(node, features.get(node), openBrowser, true, edge, nodePanelBg);
            nodePanel.add(nodeFeatures, layoutHelper.down().anchor("north").expandHoriz());
            participantSummaries.add(nodeFeatures);

            nodePanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
        }

        int thickness = edge.getNbSummarizedEdges() + 2;
        thickness = Integer.min(thickness, 25);
        content.add(new EdgeDiagram(SummaryStyle.getColor(edge.miScore), thickness, false));
    }

    @Override
    protected void fillEvidenceEdgeContent(EvidenceEdge edge) {
        createPanel(edge);
        Map<Node, List<Feature>> features = edge.getFeatures();
        for (Node node : List.of(edge.source, edge.target)) {

            JPanel nodePanel = node == edge.source ? sourcePanel : targetPanel;
            EasyGBC layoutHelper = new EasyGBC();
            nodePanel.setBackground(nodePanelBg);
            nodePanel.setOpaque(true);

            NodeBasics nodeBasics = new NodeBasics(node, openBrowser);
            nodeBasics.setBackground(nodePanelBg);
            nodePanel.add(nodeBasics, layoutHelper.down().anchor("north").expandHoriz());

            nodePanel.add(new ParticipantInfoPanel("Biological role : ",
                            node == edge.source ? edge.sourceBiologicalRole : edge.targetBiologicalRole),
                    layoutHelper.down().anchor("north").expandHoriz()
            );

            nodePanel.add(new ParticipantInfoPanel("Experimental role : ",
                            node == edge.source ? edge.sourceExperimentalRole : edge.targetExperimentalRole),
                    layoutHelper.down().anchor("north").expandHoriz()
            );

            NodeFeatures nodeFeatures = new NodeFeatures(node, features.get(node), openBrowser, false, null, nodePanelBg);
            nodePanel.add(nodeFeatures, layoutHelper.down().anchor("north").expandHoriz());

            nodePanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
        }

        content.add(new EdgeDiagram(StyleMapper.edgeTypeToPaint.get(edge.type.value), 4, edge.expansionType != null && !edge.expansionType.isBlank()));
    }

    private class ParticipantInfoPanel extends LinePanel {
        public ParticipantInfoPanel(String infoType, CVTerm term) {
            super(nodePanelBg);
            setOpaque(true);
            setAlignmentY(Component.TOP_ALIGNMENT);
            JLabel typeLabel = new JLabel(StringUtils.capitalize(infoType));
            typeLabel.setBackground(nodePanelBg);
            typeLabel.setOpaque(true);
            add(typeLabel);
            JLink valueLink = LinkUtils.createCVTermLink(openBrowser, term);
            valueLink.setBackground(nodePanelBg);
            valueLink.setOpaque(true);
            add(valueLink);
        }
    }

    public static final Map<Node, NodeDiagramInfo> nodeDiagramInfos = new Hashtable<>();

    private static class NodeDiagramInfo {
        int width;
        List<NodeDiagram> nodeDiagrams = new ArrayList<>();

        public NodeDiagramInfo(NodeDiagram nodeDiagram) {
            this.width = nodeDiagram.getPreferredSize().width;
            nodeDiagrams.add(nodeDiagram);
        }
    }

    public static void homogenizeNodeDiagramWidth() {
        while (executor.getActiveCount() != 0) {
            TimeUtils.sleep(100);
        }
        Optional<Integer> max = nodeDiagramInfos.values().stream().map(nodeDiagramInfo -> nodeDiagramInfo.width).max(Integer::compareTo);
        if (max.isPresent()) {
            int maxWidth = max.get();
            for (NodeDiagramInfo nodeDiagramInfo : nodeDiagramInfos.values()) {
                for (NodeDiagram nodeDiagram : nodeDiagramInfo.nodeDiagrams) {
                    Dimension preferredSize = nodeDiagram.getPreferredSize();
                    preferredSize.width = maxWidth;
                    nodeDiagram.setPreferredSize(preferredSize);
                    nodeDiagram.revalidate();
                    nodeDiagram.repaint();
                }
            }
        }
    }

    private void createPanel(Edge edge) {
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


        Map<Node, List<Feature>> featuresMap = edge.getFeatures();
        {
            sourcePanel = new JPanel(new GridBagLayout());
            sourcePanel.setBackground(nodePanelBg);
            sourcePanel.setBorder(border);
            sourceDiagram = new NodeDiagram(edge.source, featuresMap.get(edge.source));
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
        List<NodeDiagram> nodeDiagrams = new ArrayList<>();
        if (sourceDiagram != null) nodeDiagrams.add(sourceDiagram);
        if (targetDiagram != null) nodeDiagrams.add(targetDiagram);
        for (NodeDiagram nodeDiagram : nodeDiagrams) {
            NodeDiagramInfo nodeDiagramInfo = nodeDiagramInfos.get(nodeDiagram.node);
            if (nodeDiagramInfo == null) continue;
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
