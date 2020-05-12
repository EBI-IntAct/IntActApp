package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.edge.elements;

import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeBasics;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements.NodeSummary;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EdgeParticipants extends AbstractEdgeElement {
    private JPanel sourcePanel;
    private JPanel targetPanel;
    private static final Color nodePanelBg = new Color(229, 229, 229);
    private static final Color nodePanelBorder = new Color(186, 186, 186);
    private final List<NodeSummary> participantSummaries = new ArrayList<>();


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

            NodeSummary nodeSummary = new NodeSummary(iNode, features.get(iNode), openBrowser, true, false, false, nodePanelBg);
            nodePanel.add(nodeSummary, layoutHelper.down().anchor("north").expandHoriz());
            participantSummaries.add(nodeSummary);

            nodePanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
        }
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

            NodeSummary nodeSummary = new NodeSummary(iNode, features.get(iNode), openBrowser, false, false, false, nodePanelBg);
            nodePanel.add(nodeSummary, layoutHelper.down().anchor("north").expandHoriz());

            nodePanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
        }
    }

    private void createPanel(IntactEdge edge) {
        content.setLayout(new GridBagLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        JPanel participantsPanel = new JPanel(new GridLayout(1, 2, 2, 0));
        EasyGBC d = new EasyGBC();
        mainPanel.add(new EdgeSchematic(edge, openBrowser), d.anchor("north").expandHoriz());
        mainPanel.add(participantsPanel, d.down().expandHoriz());

        MatteBorder outsideBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, nodePanelBorder);
        MatteBorder insideBorder = BorderFactory.createMatteBorder(4, 4, 4, 4, nodePanelBg);
        CompoundBorder border = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);

        sourcePanel = new JPanel(new GridBagLayout());
        sourcePanel.setBackground(nodePanelBg);
        sourcePanel.setBorder(border);
        participantsPanel.add(sourcePanel);

        targetPanel = new JPanel(new GridBagLayout());
        targetPanel.setBackground(nodePanelBg);
        targetPanel.setBorder(border);
        participantsPanel.add(targetPanel);

        EasyGBC c = new EasyGBC();
        content.add(mainPanel, c.expandHoriz());
//        content.add(Box.createHorizontalGlue(), c.right().expandHoriz());
    }

    public void delete() {
        for (NodeSummary participantSummary : participantSummaries) {
            participantSummary.deleteEdgeSelectionCheckboxes();
        }
    }
}
