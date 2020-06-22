package uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.sub.panels.legend.panels.node;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.sub.panels.legend.panels.AbstractLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.StyleUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.CollectionUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.AbstractDetailPanel.backgroundColor;

public class NodeShapeLegendPanel extends AbstractLegendPanel {
    private final EasyGBC layoutHelper = new EasyGBC();
    private final Map<String, JPanel> nodeShapesLines = new HashMap<>();


    public NodeShapeLegendPanel(IntactManager manager, IntactNetwork currentINetwork, IntactNetworkView currentIView) {
        super("<html>Node Shape <em>~ Interactor Type</em></html>", manager, currentINetwork, currentIView);
        for (String nodeType : StyleMapper.originalNodeTypeToShape.keySet()) {
            JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,4,2));
            linePanel.setBackground(backgroundColor);
            linePanel.add(StyleUtils.nodeTypeToShape(nodeType, Color.lightGray, 30));
            JLabel label = new JLabel(StringUtils.capitalize(nodeType));
            label.setBorder(new EmptyBorder(0, 4, 0, 0));
            linePanel.add(label);
            content.add(linePanel, layoutHelper.down().expandHoriz());
            nodeShapesLines.put(nodeType, linePanel);
        }
    }

    @Override
    public void filterCurrentLegend() {
        executor.execute(() -> {
            Set<String> networkInteractorTypes = currentINetwork.getInteractorTypes();

            for (String nodeType : nodeShapesLines.keySet()) {
                nodeShapesLines.get(nodeType).setVisible(
                        networkInteractorTypes.contains(nodeType) ||
                                (StyleMapper.nodeTypeToParent.containsKey(nodeType) && CollectionUtils.anyCommonElement(networkInteractorTypes, StyleMapper.nodeTypeToParent.get(nodeType)))
                );
            }

        });
    }
}
