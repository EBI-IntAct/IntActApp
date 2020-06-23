package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.intact.app.internal.ui.components.legend.EdgeLegend;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractDetailPanel;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ExpandedEdgeLegendPanel extends AbstractEdgeLegendPanel {
    public ExpandedEdgeLegendPanel() {
        super();
        add(createEdgeShapePanel(), layoutHelper.down().anchor("west").expandHoriz());
        add(createEdgeColorPanel(), layoutHelper.down().anchor("west").expandHoriz());

    }

    protected CollapsablePanel createEdgeShapePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        EasyGBC d = new EasyGBC();
        panel.setBackground(AbstractDetailPanel.backgroundColor);
        {
            JPanel linePanel = new LinePanel(AbstractDetailPanel.backgroundColor);
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
        panel.setBackground(AbstractDetailPanel.backgroundColor);

        String[] types = {"colocalization", "association", "physical association", "direct interaction", "phosphorylation", "dephosphorylation"};

        for (String type : types) {
            JPanel linePanel = new LinePanel(AbstractDetailPanel.backgroundColor);
            linePanel.add(new EdgeLegend(StyleMapper.edgeTypeToPaint.get(type)));
            JLabel label = new JLabel(StringUtils.capitalize(type));
            label.setBorder(new EmptyBorder(0, 4, 0, 0));
            linePanel.add(label);
            panel.add(linePanel, d.anchor("west").down().expandHoriz());
        }

        return new CollapsablePanel("<html><nobr>Edge Color <em>~ Interaction type</em></nobr></html>", panel, false);
    }
}
