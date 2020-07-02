package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.managers.Manager;
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
    protected Network currentINetwork;
    protected NetworkView currentIView;
    protected ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public AbstractLegendPanel(String text, Manager manager, Network currentINetwork, NetworkView currentIView) {
        super(text, false);
        this.manager = manager;
        this.currentINetwork = currentINetwork;
        this.currentIView = currentIView;
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

    public void networkChanged(Network newINetwork) {
        this.currentINetwork = newINetwork;
    }

    public void networkViewChanged(NetworkView newINetworkView) {
        currentIView = newINetworkView;
    }
}
