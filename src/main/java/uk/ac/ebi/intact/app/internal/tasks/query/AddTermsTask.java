package uk.ac.ebi.intact.app.internal.tasks.query;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.IntactNetwork;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution.ResolveTermsPanel;

import javax.swing.*;

public class AddTermsTask extends AbstractTask {
    final IntactManager manager;
    final CyNetwork network;
    CyNetworkView netView;

    public AddTermsTask(final IntactManager manager, final CyNetwork network,
                        CyNetworkView netView) {
        this.manager = manager;
        this.network = network;
        this.netView = netView;
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("Query for additional nodes");
        IntactNetwork intactNetwork = manager.data.getIntactNetwork(network);
        JFrame parent = manager.utils.getService(CySwingApplication.class).getJFrame();
        // Get AddTerms dialog
        JDialog termsDialog = new JDialog(parent, "Query for additional nodes");
        ResolveTermsPanel termsPanel = new ResolveTermsPanel(manager, intactNetwork);
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
