package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.legend.panels.edge;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.ui.components.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.legend.EdgeLegend;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;

public class ExpandedEdgeLegendPanel extends AbstractEdgeLegendPanel {
    public ExpandedEdgeLegendPanel() {
        super();
        add(createEdgeShapePanel(), layoutHelper.down().anchor("west").expandHoriz());
        add(createEdgeColorPanel(), layoutHelper.down().anchor("west").expandHoriz());

    }

    protected CollapsablePanel createEdgeShapePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        EasyGBC d = new EasyGBC();
        panel.setBackground(backgroundColor);
        {
            JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            linePanel.setBackground(backgroundColor);
            linePanel.add(new EdgeLegend(EdgeLegend.LineType.DASHED));
            JLabel label = new JLabel("Spoke expanded");
            label.setBorder(new EmptyBorder(0, 4, 0, 0));
            linePanel.add(label);
            panel.add(linePanel, d.anchor("west").down().expandHoriz());
        }

        panel.add(Box.createVerticalStrut(5));
        return new CollapsablePanel("<html><nobr>Edge Shape <em>~ Spoke expansion</em></nobr></html>", panel, false);
    }

    protected CollapsablePanel createEdgeColorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        EasyGBC d = new EasyGBC();
        panel.setBackground(backgroundColor);

        String[] types = {"colocalization", "association", "physical association", "direct interaction", "phosphorylation", "dephosphorylation"};

        for (String type : types) {
            JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            linePanel.setBackground(backgroundColor);
            linePanel.add(new EdgeLegend(StyleMapper.edgeTypeToPaint.get(type)));
            JLabel label = new JLabel(StringUtils.capitalize(type));
            label.setBorder(new EmptyBorder(0, 4, 0, 0));
            linePanel.add(label);
            panel.add(linePanel, d.anchor("west").down().expandHoriz());
        }

        return new CollapsablePanel("<html><nobr>Edge Color <em>~ Interaction type</em></nobr></html>", panel, false);
    }
}
