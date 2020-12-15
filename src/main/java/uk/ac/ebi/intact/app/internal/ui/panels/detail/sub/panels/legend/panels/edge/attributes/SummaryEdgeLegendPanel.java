package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.attributes;

import uk.ac.ebi.intact.app.internal.model.styles.SummaryStyle;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.components.legend.ContinuousColorLegend;
import uk.ac.ebi.intact.app.internal.ui.components.legend.ContinuousEdgeWidthLegend;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.slider.MIScoreSliderUI;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.AbstractEdgeElementPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SummaryEdgeLegendPanel extends AbstractEdgeElementPanel {
    public SummaryEdgeLegendPanel() {
        super();
        add(createEdgeColorPanel(), layoutHelper.down().anchor("west").expandHoriz());
        add(createEdgeWidthPanel(), layoutHelper.down().anchor("west").expandHoriz());
    }

    protected CollapsablePanel createEdgeColorPanel() {
        JPanel edgeColorPanel = new JPanel(new GridBagLayout());
        edgeColorPanel.setBackground(UIColors.lightBackground);
        {
            EasyGBC d = new EasyGBC();
            edgeColorPanel.setBackground(UIColors.lightBackground);
            ContinuousColorLegend miScoreLegend = new ContinuousColorLegend(MIScoreSliderUI.colors, MIScoreSliderUI.floats, 10);
            miScoreLegend.setBackground(UIColors.lightBackground);
            miScoreLegend.setBorder(new EmptyBorder(5, 0, 10, 15));
            edgeColorPanel.add(miScoreLegend, d.down().anchor("west").expandHoriz());
        }
        return new CollapsablePanel("<html>Edge Color <em>~ MI Score</em></html>", edgeColorPanel, false);
    }

    protected CollapsablePanel createEdgeWidthPanel() {
        JPanel edgeWidthPanel = new JPanel(new GridBagLayout());
        edgeWidthPanel.setBackground(UIColors.lightBackground);
        {
            EasyGBC d = new EasyGBC();
            edgeWidthPanel.setBackground(UIColors.lightBackground);
            ContinuousEdgeWidthLegend edgeWidthLegend = new ContinuousEdgeWidthLegend(SummaryStyle.edgeWidth1, SummaryStyle.edgeWidth2, SummaryStyle.edgeWidthValue1, SummaryStyle.edgeWidthValue2);
            edgeWidthLegend.setBackground(UIColors.lightBackground);
            edgeWidthLegend.setBorder(new EmptyBorder(5, 0, 0, 15));
            edgeWidthPanel.add(edgeWidthLegend, d.down().anchor("west").expandHoriz());
        }
        return new CollapsablePanel("<html>Edge Width <em>~ # Evidence</em></html>", edgeWidthPanel, false);
    }
}
