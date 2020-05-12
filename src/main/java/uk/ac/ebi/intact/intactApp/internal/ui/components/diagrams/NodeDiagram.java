package uk.ac.ebi.intact.intactApp.internal.ui.components.diagrams;

import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.FeatureClassifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.styles.IntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.MutationIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.ui.components.JLabel2D;
import uk.ac.ebi.intact.intactApp.internal.ui.components.legend.shapes.AbstractNodeShape;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.StyleUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.CollectionUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;

public class NodeDiagram extends JPanel {
    private final JPanel shapePanel = new JPanel();

    public NodeDiagram(IntactNode iNode, List<Feature> features) {
        this.setBackground(new Color(0,0,0,0));
        this.setOpaque(false);
        this.setLayout(new OverlayLayout(this));
        this.setAlignmentX(LEFT_ALIGNMENT);
        if (iNode.name != null && !iNode.name.isBlank()) {
            JLabel2D label = new JLabel2D(iNode.name, JLabel.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            label.setForeground(Color.WHITE);
            label.setOutlineColor(new Color(0, 0, 0, 95));
            label.setStroke(5);
            label.setAlignmentX(CENTER_ALIGNMENT);
            label.setAlignmentY(CENTER_ALIGNMENT);
            this.add(label);
        }


        Color color = (Color) StyleMapper.taxIdToPaint.get(iNode.taxId);
        if (color == null) {
            color = (Color) StyleMapper.kingdomColors.get(iNode.taxId);
        }
        shapePanel.setBackground(backgroundColor);
        AbstractNodeShape shape = StyleUtils.nodeTypeToShape(iNode.type, color != null ? color : IntactStyle.defaultNodeColor, 50);

        Set<String> featureMIIDs = features.stream().map(feature -> feature.typeMIId).collect(Collectors.toSet());
        if (CollectionUtils.anyCommonElement(featureMIIDs, FeatureClassifier.mutation.innerMIIds)) {
            shape.setBorderColor(MutationIntactStyle.mutatedColor);
            shape.setBorderThickness(4);
        }

        shapePanel.setAlignmentX(CENTER_ALIGNMENT);
        shapePanel.setAlignmentY(CENTER_ALIGNMENT);
        shapePanel.add(shape);
        this.add(shapePanel);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (shapePanel != null) {
            shapePanel.setBackground(bg);
        }
    }
}
