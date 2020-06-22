package uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.sub.panels.legend.panels.edge;

import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.legend.ContinuousColorLegend;
import uk.ac.ebi.intact.intactApp.internal.ui.components.legend.ContinuousEdgeWidthLegend;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.MIScoreSliderUI;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static uk.ac.ebi.intact.intactApp.internal.model.styles.CollapsedIntactStyle.*;
import static uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.AbstractDetailPanel.backgroundColor;

public class CollapsedEdgeLegendPanel extends AbstractEdgeLegendPanel {
    public CollapsedEdgeLegendPanel() {
        super();
        add(createEdgeColorPanel(), layoutHelper.down().anchor("west").expandHoriz());
        add(createEdgeWidthPanel(), layoutHelper.down().anchor("west").expandHoriz());
    }

    protected CollapsablePanel createEdgeColorPanel() {
        JPanel edgeColorPanel = new JPanel(new GridBagLayout());
        edgeColorPanel.setBackground(backgroundColor);
        {
            EasyGBC d = new EasyGBC();
            edgeColorPanel.setBackground(backgroundColor);
            ContinuousColorLegend miScoreLegend = new ContinuousColorLegend(MIScoreSliderUI.colors, MIScoreSliderUI.floats, 10);
            miScoreLegend.setBackground(backgroundColor);
            miScoreLegend.setBorder(new EmptyBorder(5, 0, 10, 15));
            edgeColorPanel.add(miScoreLegend, d.down().anchor("west").expandHoriz());
        }
        return new CollapsablePanel("<html>Edge Color <em>~ MI Score</em></html>", edgeColorPanel, false);
    }

    protected CollapsablePanel createEdgeWidthPanel() {
        JPanel edgeWidthPanel = new JPanel(new GridBagLayout());
        edgeWidthPanel.setBackground(backgroundColor);
        {
            EasyGBC d = new EasyGBC();
            edgeWidthPanel.setBackground(backgroundColor);
            ContinuousEdgeWidthLegend edgeWidthLegend = new ContinuousEdgeWidthLegend(edgeWidth1, edgeWidth2, edgeWidthValue1, edgeWidthValue2);
            edgeWidthLegend.setBackground(backgroundColor);
            edgeWidthLegend.setBorder(new EmptyBorder(5, 0, 0, 15));
            edgeWidthPanel.add(edgeWidthLegend, d.down().anchor("west").expandHoriz());
        }
        return new CollapsablePanel("<html>Edge Width <em>~ # Evidence</em></html>", edgeWidthPanel, false);
    }
}
