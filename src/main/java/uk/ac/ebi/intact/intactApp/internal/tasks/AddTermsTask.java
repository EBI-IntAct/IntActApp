package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.ui.GetTermsPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

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
        IntactNetwork intactNetwork = manager.getStringNetwork(network);
        JFrame parent = manager.getService(CySwingApplication.class).getJFrame();
        // Get AddTerms dialog
        JDialog termsDialog = new JDialog(parent, "Query for additional nodes");
        String database = ModelUtils.getDatabase(network);
        String species = ModelUtils.getNetSpecies(network);
        GetTermsPanel termsPanel = new GetTermsPanel(manager, intactNetwork, database, species, true);
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
