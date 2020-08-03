package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels;

import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.events.StyleUpdatedListener;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.components.legend.NodeColorLegendEditor;
import uk.ac.ebi.intact.app.internal.ui.components.spinner.LoadingSpinner;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.EdgeLegendPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.NodeLegendPanel;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;

public class LegendDetailPanel extends AbstractDetailPanel implements StyleUpdatedListener {
    private final NodeLegendPanel nodePanel;
    private final EdgeLegendPanel edgePanel;
    private final LoadingSpinner loadingSpinner = new LoadingSpinner();

    public LegendDetailPanel(final Manager manager) {
        super(manager, 0, "legend");
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIColors.lightBackground);
        JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        setLayout(new GridBagLayout());
        StyleMapper.addStyleUpdatedListener(this);

        nodePanel = new NodeLegendPanel(manager, currentNetwork, currentView);
        edgePanel = new EdgeLegendPanel(manager, currentNetwork, currentView);

        EasyGBC layoutHelper = new EasyGBC();
        mainPanel.add(createResetStyleButton(), layoutHelper.anchor("northwest").noExpand());
        mainPanel.add(loadingSpinner, layoutHelper.down().noExpand());
        mainPanel.add(nodePanel, layoutHelper.down().anchor("west").expandHoriz());
        mainPanel.add(edgePanel, layoutHelper.down().anchor("west").expandHoriz());
        mainPanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
        add(scrollPane, new EasyGBC().down().anchor("west").expandBoth());
        if (currentNetwork != null && currentView != null) {
            filterCurrentLegends();
        }
    }


    private JButton createResetStyleButton() {
        JButton resetStylesButton = new JButton("Reset styles");

        resetStylesButton.addActionListener(e -> new Thread(() -> {
            resetStylesButton.setEnabled(false);
            loadingSpinner.start();

            NodeColorLegendEditor.clearAll(false);
            manager.style.resetStyles(false);
            StyleMapper.originalKingdomColors.forEach((taxId, paint) -> nodePanel.nodeColorLegendPanel.colorPickers.get(taxId).setCurrentColor((Color) paint));
            StyleMapper.originalTaxIdToPaint.forEach((taxId, paint) -> nodePanel.nodeColorLegendPanel.colorPickers.get(taxId).setCurrentColor((Color) paint));

            loadingSpinner.stop();
            resetStylesButton.setEnabled(true);
        }).start());
        return resetStylesButton;
    }

    public void networkViewChanged(CyNetworkView view) {
        NetworkView networkView = manager.data.getNetworkView(view);
        if (networkView != null) {
            currentView = networkView;
            currentNetwork = currentView.network;

            filterCurrentLegends();

            viewUpdated(currentView.getType());
            nodePanel.networkViewChanged(currentView);
            edgePanel.networkViewChanged(currentView);
        }
    }

    private void filterCurrentLegends() {
        if (nodePanel != null && edgePanel != null) {
            nodePanel.filterCurrentLegend();
            edgePanel.filterCurrentLegend();
        }
    }

    public void viewUpdated(NetworkView.Type newType) {
        nodePanel.viewTypeChanged(newType);
        edgePanel.viewTypeChanged(newType);
    }

    public void networkChanged(Network newNetwork) {
        currentNetwork = newNetwork;
        nodePanel.networkChanged(newNetwork);
        edgePanel.networkChanged(newNetwork);
        for (NodeColorLegendEditor nodeColorLegendEditor : NodeColorLegendEditor.getNodeColorLegendEditorList()) {
            nodeColorLegendEditor.networkChanged(newNetwork);
        }
    }

    @Override
    public void handleStyleUpdatedEvent() {
        filterCurrentLegends();
    }
}

