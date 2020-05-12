package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.legend.panels.edge;

import uk.ac.ebi.intact.intactApp.internal.model.styles.MutationIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.ui.components.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.legend.EdgeLegend;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;

public class MutationEdgeLegendPanel extends AbstractEdgeLegendPanel {

    public MutationEdgeLegendPanel() {
        super();
        add(createEdgeShapeLegendPanel(), layoutHelper.down().anchor("west").expandHoriz());
        add(createEdgeColorAndWidthPanel(), layoutHelper.down().anchor("west").expandHoriz());
    }

    protected CollapsablePanel createEdgeShapeLegendPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        EasyGBC d = new EasyGBC();
        panel.setBackground(backgroundColor);

        {
            JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            linePanel.setBackground(backgroundColor);
            EdgeLegend dashed = new EdgeLegend(EdgeLegend.LineType.DASHED);
            dashed.setPaint(MutationIntactStyle.wildColor);
            dashed.setThickness(2);
            linePanel.add(dashed);
            JLabel label = new JLabel("Spoke expanded");
            label.setBorder(new EmptyBorder(0, 4, 0, 0));
            linePanel.add(label);
            panel.add(linePanel, d.anchor("west").down().expandHoriz());
        }

        panel.add(Box.createVerticalStrut(8));
        return new CollapsablePanel("<html><nobr>Edge Shape <em>~ Spoke expansion</em></nobr></html>", panel, false);

    }

    protected CollapsablePanel createEdgeColorAndWidthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        EasyGBC d = new EasyGBC();
        panel.setBackground(backgroundColor);

        {
            JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            linePanel.setBackground(backgroundColor);
            EdgeLegend mutated = new EdgeLegend(MutationIntactStyle.mutatedColor);
            linePanel.add(mutated);
            JLabel label = new JLabel("Affected by mutation");
            label.setBorder(new EmptyBorder(0, 4, 0, 0));
            linePanel.add(label);
            panel.add(linePanel, d.anchor("west").down().expandHoriz());
        }

        return new CollapsablePanel("<html><nobr>Edge Color & Width <em>~ Affected by mutation</em></nobr></html>", panel, false);
    }
}
