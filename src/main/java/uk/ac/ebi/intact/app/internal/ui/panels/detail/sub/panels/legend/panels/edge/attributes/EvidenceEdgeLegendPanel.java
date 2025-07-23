package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.attributes;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.filters.edge.EdgeTypeFilter;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions.InteractionType;
import uk.ac.ebi.intact.app.internal.ui.components.legend.EdgeLegend;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge.AbstractEdgeElementPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EvidenceEdgeLegendPanel extends AbstractEdgeElementPanel {
    private Map<String, Component> edgeTypeToRepresentation = new HashMap<>();

    public EvidenceEdgeLegendPanel() {
        super();
        add(createEdgeShapePanel(), layoutHelper.down().anchor("west").expandHoriz());
        add(createEdgeColorPanel(), layoutHelper.down().anchor("west").expandHoriz());

    }

    protected CollapsablePanel createEdgeShapePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        EasyGBC d = new EasyGBC();
        panel.setBackground(UIColors.lightBackground);
        {
            JPanel linePanel = new LinePanel(UIColors.lightBackground);
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
        panel.setBackground(UIColors.lightBackground);

        for (InteractionType type : InteractionType.getLegendTypes()) {
            JPanel linePanel = new LinePanel(UIColors.lightBackground);
            linePanel.add(new EdgeLegend(StyleMapper.edgeTypeToPaint.get(type.name)));
            JLabel label = new JLabel(StringUtils.capitalize(type.name));
            label.setBorder(new EmptyBorder(0, 4, 0, 0));
            linePanel.add(label);
            edgeTypeToRepresentation.put(type.name, linePanel);
            panel.add(linePanel, d.anchor("west").down().expandHoriz());
        }

        return new CollapsablePanel("<html><nobr>Edge Color <em>~ Interaction type</em></nobr></html>", panel, false);
    }

    public void filterLegendWithCurrent(NetworkView currentView) {
        if (currentView == null) return;
        Set<String> currentEdgeTypes = currentView.getPropertyValuesOfFilter(EdgeTypeFilter.class);
        if (currentEdgeTypes == null) return;
        edgeTypeToRepresentation.forEach((edgeType, edgeRepresentation) -> edgeRepresentation.setVisible(currentEdgeTypes.contains(edgeType)
                || (StyleMapper.edgeTypeToChildren.containsKey(edgeType) && CollectionUtils.anyCommonElement(currentEdgeTypes, StyleMapper.edgeTypeToChildren.get(edgeType))))
        );
    }
}
