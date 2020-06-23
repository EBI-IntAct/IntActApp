package uk.ac.ebi.intact.app.internal.tasks.query;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;

public class RequeryTask extends AbstractTask {

    final IntactManager manager;
    final CyNetwork network;
    //private JButton btnOK = null;
    //private JButton btnCancel = null;
    //private JDialog confirmDialog = null;
    private final Logger logger = Logger.getLogger(CyUserLog.NAME);
    private TaskMonitor monitor;

    // @Tunable(description = "Get the latest STRING network for your nodes", gravity = 1.0,
    // required = true)
    // public boolean requery = true;

    // @Tunable(description = "Get the latest STRING network for your nodes", gravity = 1.0,
    // required = true)
    // public UserAction requeryBtn = new UserAction(this);

    public RequeryTask(final IntactManager manager, CyNetwork network) {
        this.manager = manager;
        this.network = network;
    }

    public void run(TaskMonitor aMonitor) {
        this.monitor = aMonitor;
        monitor.setTitle("Re-query network");
//        StringifyTask strTask = new StringifyTask(manager, network, conf,
//                Species.getSpecies(sp), ModelUtils.CANONICAL);
//        strTask.run(monitor);
    }

    @ProvidesTitle
    public String getTitle() {
        return "Re-query network";
    }

    @Override
    public void cancel() {

    }

    // @Override
    // public void actionPerformed(ActionEvent e) {
    // if (e.getSource().equals(btnOK)) {
    // // TODO: check for species and cutoff!
    // StringifyTask strTask = new StringifyTask(manager, network, 0.4,
    // Species.getSpecies("Homo sapiens"), "canonical name");
    // strTask.run(monitor);
    // }
    // confirmDialog.dispose();
    // }

}
