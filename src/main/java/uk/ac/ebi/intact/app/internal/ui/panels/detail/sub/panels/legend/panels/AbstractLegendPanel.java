package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class AbstractLegendPanel extends CollapsablePanel {
    protected Manager manager;
    protected EasyGBC layoutHelper = new EasyGBC();
    protected Network currentNetwork;
    protected NetworkView currentView;
    protected ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public AbstractLegendPanel(String text, Manager manager, Network currentNetwork, NetworkView currentView) {
        super(text, false);
        this.manager = manager;
        this.currentNetwork = currentNetwork;
        this.currentView = currentView;
        content.setLayout(new GridBagLayout());
        setBackground(UIColors.lightBackground);
    }

    public void addSeparator() {
        JSeparator separator;
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.getPreferredSize().height = 1;
        content.add(separator, layoutHelper.down().anchor("west").expandHoriz());
    }


    public abstract void filterCurrentLegend();

    public void networkChanged(Network newNetwork) {
        this.currentNetwork = newNetwork;
    }

    public void networkViewChanged(NetworkView newNetworkView) {
        currentView = newNetworkView;
    }
}
