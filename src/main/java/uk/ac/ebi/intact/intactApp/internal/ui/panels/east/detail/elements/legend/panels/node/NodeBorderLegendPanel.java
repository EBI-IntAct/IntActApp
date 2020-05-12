package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.legend.panels.node;

import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.styles.MutationIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.ui.components.legend.shapes.Ball;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.legend.panels.AbstractLegendPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;

public class NodeBorderLegendPanel extends AbstractLegendPanel {

    public NodeBorderLegendPanel(IntactManager manager, IntactNetwork currentINetwork, IntactNetworkView currentIView) {
        super("<html>Node Border <em>~ Mutation</em></html>", manager, currentINetwork, currentIView);
        JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,4,2));
        linePanel.setBackground(backgroundColor);

        Ball mutated = new Ball(Color.lightGray, 30);
        mutated.setBorderColor(MutationIntactStyle.mutatedColor);
        mutated.setBorderThickness(4);
        linePanel.add(mutated);
        JLabel label = new JLabel("Mutated  interactor");
        label.setBorder(new EmptyBorder(0, 4, 0, 0));
        linePanel.add(label);
        content.add(linePanel, layoutHelper.expandHoriz());
    }

    @Override
    public void filterCurrentLegend() {

    }
}
