package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements;

import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.ui.components.legend.NodeColorLegendEditor;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.AbstractDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.EdgeLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.NodeLegendPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;

public class LegendDetailPanel extends AbstractDetailPanel  {
    private final NodeLegendPanel nodePanel;
    private final EdgeLegendPanel edgePanel;

    public LegendDetailPanel(final IntactManager manager) {
        super(manager);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(backgroundColor);
        JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        setLayout(new GridBagLayout());

        nodePanel = new NodeLegendPanel(manager, currentINetwork, currentIView);
        edgePanel = new EdgeLegendPanel(manager, currentINetwork, currentIView);

        EasyGBC layoutHelper = new EasyGBC();
        mainPanel.add(createResetStyleButton(), layoutHelper.anchor("north"));
        mainPanel.add(nodePanel, layoutHelper.down().anchor("west").expandHoriz());
        mainPanel.add(edgePanel, layoutHelper.down().anchor("west").expandHoriz());
        mainPanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
        add(scrollPane, new EasyGBC().down().anchor("west").expandBoth());
        filterCurrentLegends();
    }


    private JButton createResetStyleButton() {
        JButton resetStylesButton = new JButton("Reset styles");
        resetStylesButton.addActionListener(e -> new Thread(() -> {
            manager.resetStyles();
            StyleMapper.originalKingdomColors.forEach((taxId, paint) -> nodePanel.nodeColorLegendPanel.colorPickers.get(taxId).setCurrentColor((Color) paint));
            StyleMapper.originalTaxIdToPaint.forEach((taxId, paint) -> nodePanel.nodeColorLegendPanel.colorPickers.get(taxId).setCurrentColor((Color) paint));
        }).start());
        return resetStylesButton;
    }

    @Override
    protected void doFilter(String type) {
    }

    public void networkViewChanged(CyNetworkView view) {
        currentIView = manager.getIntactNetworkView(view);
        currentINetwork = currentIView.getNetwork();

        filterCurrentLegends();

        viewTypeChanged(currentIView.getType());
        nodePanel.networkViewChanged(currentIView);
        edgePanel.networkViewChanged(currentIView);
    }

    private void filterCurrentLegends() {
        nodePanel.filterCurrentLegend();
        edgePanel.filterCurrentLegend();
    }

    public void viewTypeChanged(IntactNetworkView.Type newType) {
        nodePanel.viewTypeChanged(newType);
        edgePanel.viewTypeChanged(newType);
    }

    public void networkChanged(IntactNetwork newNetwork) {
        nodePanel.networkChanged(newNetwork);
        edgePanel.networkChanged(newNetwork);
        for (NodeColorLegendEditor nodeColorLegendEditor : NodeColorLegendEditor.getNodeColorLegendEditorList()) {
            nodeColorLegendEditor.networkChanged(newNetwork);
        }
    }
}

