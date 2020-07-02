package uk.ac.ebi.intact.app.internal.tasks.query;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution.ResolveTermsPanel;

import javax.swing.*;

public class AddTermsTask extends AbstractTask {
    final Manager manager;
    final CyNetwork network;
    CyNetworkView netView;

    public AddTermsTask(final Manager manager, final CyNetwork network,
                        CyNetworkView netView) {
        this.manager = manager;
        this.network = network;
        this.netView = netView;
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("Query for additional nodes");
        Network network = manager.data.getNetwork(this.network);
        JFrame parent = manager.utils.getService(CySwingApplication.class).getJFrame();
        // Get AddTerms dialog
        JDialog termsDialog = new JDialog(parent, "Query for additional nodes");
        ResolveTermsPanel termsPanel = new ResolveTermsPanel(manager, network);
        termsDialog.setContentPane(termsPanel);
        // Pack it and display it
        termsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        termsDialog.pack();
        termsDialog.setVisible(true);
    }

    @ProvidesTitle
    public String getTitle() {
        return "Query for additional nodes";
    }
}
