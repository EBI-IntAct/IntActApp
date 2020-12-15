package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.node.elements;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.MutationStyle;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.components.legend.shapes.Ball;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.AbstractLegendPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NodeBorderLegendPanel extends AbstractLegendPanel {

    public NodeBorderLegendPanel(Manager manager, Network currentNetwork, NetworkView currentView) {
        super("<html>Node Border <em>~ Mutation</em></html>", manager, currentNetwork, currentView);
        JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,4,2));
        linePanel.setBackground(UIColors.lightBackground);

        Ball mutated = new Ball(Color.lightGray, 30);
        mutated.setBorderColor(MutationStyle.mutatedColor);
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
