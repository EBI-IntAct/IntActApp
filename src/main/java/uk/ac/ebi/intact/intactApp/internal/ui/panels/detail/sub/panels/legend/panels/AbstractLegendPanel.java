package uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.sub.panels.legend.panels;

import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.AbstractDetailPanel.backgroundColor;

public abstract class AbstractLegendPanel extends CollapsablePanel {
    protected IntactManager manager;
    protected EasyGBC layoutHelper = new EasyGBC();
    protected IntactNetwork currentINetwork;
    protected IntactNetworkView currentIView;
    protected ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public AbstractLegendPanel(String text, IntactManager manager, IntactNetwork currentINetwork, IntactNetworkView currentIView) {
        super(text, false);
        this.manager = manager;
        this.currentINetwork = currentINetwork;
        this.currentIView = currentIView;
        content.setLayout(new GridBagLayout());
        setBackground(backgroundColor);
    }

    public void addSeparator() {
        JSeparator separator;
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.getPreferredSize().height = 1;
        content.add(separator, layoutHelper.down().anchor("west").expandHoriz());
    }


    public abstract void filterCurrentLegend();

    public void networkChanged(IntactNetwork newINetwork) {
        this.currentINetwork = newINetwork;
    }

    public void networkViewChanged(IntactNetworkView newINetworkView) {
        currentIView = newINetworkView;
    }
}
